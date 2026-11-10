package br.com.pokedex.seed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê os comandos {@code INSERT INTO ... VALUES (...), (...);} de um script SQL e devolve
 * as linhas de cada tabela. É assim que o {@code Script_Pokedex.sql} vira fonte de dados
 * para o MongoDB, sem precisar de MySQL.
 *
 * <p>Suporta números, {@code NULL} e strings entre aspas simples com escape {@code ''}
 * (ex.: {@code 'Farfetch''d'}). Os demais comandos (CREATE TABLE, SELECT) são ignorados.
 */
public final class SqlScriptParser {

    private static final Pattern INSERT = Pattern.compile(
            "INSERT\\s+INTO\\s+`?(\\w+)`?\\s*\\(([^)]*)\\)\\s*VALUES",
            Pattern.CASE_INSENSITIVE);

    /** Linhas de uma tabela, com os nomes das colunas na mesma ordem dos valores. */
    public record Table(String name, List<String> columns, List<List<Object>> rows) {

        /** Cada linha como mapa coluna → valor. */
        public List<Map<String, Object>> rowsAsMaps() {
            return rows.stream().map(row -> {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    map.put(columns.get(i), row.get(i));
                }
                return map;
            }).toList();
        }
    }

    private SqlScriptParser() {
    }

    /** @return tabelas encontradas, indexadas pelo nome (como aparece no script) */
    public static Map<String, Table> parse(String sql) {
        Map<String, Table> tables = new LinkedHashMap<>();
        Matcher matcher = INSERT.matcher(sql);
        int position = 0;

        while (matcher.find(position)) {
            String tableName = matcher.group(1);
            List<String> columns = Arrays.stream(matcher.group(2).split(","))
                    .map(c -> c.replace("`", "").trim())
                    .toList();

            Cursor cursor = new Cursor(sql, matcher.end());
            List<List<Object>> rows = parseValues(cursor, tableName, columns.size());
            tables.computeIfAbsent(tableName, n -> new Table(n, columns, new ArrayList<>()))
                    .rows().addAll(rows);
            position = cursor.index;
        }
        return tables;
    }

    /** Lê {@code (v1, v2), (v1, v2) ;} a partir da posição atual. */
    private static List<List<Object>> parseValues(Cursor cursor, String table, int columnCount) {
        List<List<Object>> rows = new ArrayList<>();
        while (true) {
            cursor.skipWhitespace();
            if (cursor.atEnd() || cursor.peek() == ';') {
                cursor.index++;
                return rows;
            }
            if (cursor.peek() == ',') {
                cursor.index++;
                continue;
            }
            if (cursor.peek() != '(') {
                // Fim do INSERT sem ';' (próximo comando começou).
                return rows;
            }
            List<Object> row = parseTuple(cursor);
            if (row.size() != columnCount) {
                throw new IllegalStateException("Linha da tabela %s com %d valores (esperado %d): %s"
                        .formatted(table, row.size(), columnCount, row));
            }
            rows.add(row);
        }
    }

    private static List<Object> parseTuple(Cursor cursor) {
        List<Object> values = new ArrayList<>();
        cursor.index++; // '('
        while (true) {
            cursor.skipWhitespace();
            char c = cursor.peek();
            if (c == '\'') {
                values.add(cursor.readQuotedString());
            } else {
                values.add(toValue(cursor.readUntilAny(",)")));
            }
            cursor.skipWhitespace();
            char separator = cursor.peek();
            cursor.index++;
            if (separator == ')') {
                return values;
            }
        }
    }

    private static Object toValue(String token) {
        String raw = token.trim();
        if (raw.equalsIgnoreCase("NULL")) {
            return null;
        }
        if (raw.matches("-?\\d+")) {
            return Integer.valueOf(raw);
        }
        if (raw.matches("-?\\d+\\.\\d+")) {
            return Double.valueOf(raw);
        }
        return raw;
    }

    /** Ponteiro simples sobre o texto do script. */
    private static final class Cursor {
        private final String text;
        private int index;

        Cursor(String text, int index) {
            this.text = text;
            this.index = index;
        }

        boolean atEnd() {
            return index >= text.length();
        }

        char peek() {
            if (atEnd()) {
                throw new IllegalStateException("Fim inesperado do script SQL");
            }
            return text.charAt(index);
        }

        void skipWhitespace() {
            while (!atEnd() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        String readQuotedString() {
            StringBuilder sb = new StringBuilder();
            index++; // aspas de abertura
            while (true) {
                char c = peek();
                index++;
                if (c == '\'') {
                    if (!atEnd() && text.charAt(index) == '\'') { // '' → '
                        sb.append('\'');
                        index++;
                    } else {
                        return sb.toString();
                    }
                } else if (c == '\\' && !atEnd()) { // escape estilo MySQL: \'
                    sb.append(text.charAt(index++));
                } else {
                    sb.append(c);
                }
            }
        }

        String readUntilAny(String stops) {
            int start = index;
            while (!atEnd() && stops.indexOf(text.charAt(index)) < 0) {
                index++;
            }
            return text.substring(start, index);
        }
    }
}
