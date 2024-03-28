/*
 * Copyright (C) 2024 Syntarou YOSHIDA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.midimixer.libs.json;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class JsonParser {

    public JsonParser() {
        _reader = null;

    }

    public JsonParser(File file) throws IOException {
        this();
        parse(file);

    }

    public void parse(String contents) {
        _reader = new JsonReader(contents);
        parse(_reader);
    }

    public void parse(File file) throws IOException {
        _reader = new JsonReader(file);
        parse(_reader);
    }

    ArrayList<String> _parse1 = null;

    protected void parse(JsonReader reader) {
        int point = 0;
        int total = 0;
        _tree = null;
        _parse1 = new ArrayList<>();
        while (true) {
            String original = reader.readPartial();
            if (original == null) {
                break;
            }
            _parse1.add(original);
            if (true) {
                String escaped1 = original;
                if (original.startsWith("\"")) {
                    //ok
                } else {
                    escaped1 = JsonReader.encape(original);
                }
                String unescaped1 = JsonReader.unescape(escaped1);
                String escaped2 = JsonReader.encape(unescaped1);
                String unescaped2 = JsonReader.unescape(escaped2);

                if (escaped1.equals(escaped2) == false || unescaped2.equals(unescaped1) == false) {
                    System.out.println("escaped1[" + escaped1 + "]");
                    System.out.println("escaped2[" + escaped2 + "]");
                    System.out.println("unescaped1[" + unescaped1 + "]");
                    System.out.println("unescaped2[" + unescaped2 + "]");
                    total++;
                } else {
                    point++;
                    total++;
                }
            }
        }
        //System.out.println("point " + point + " total  " + total);
        LinkedList<JValue> queue = new LinkedList<>();
        for (String seek : _parse1) {
            queue.add(new JValue(seek));
        }
        try {
            _tree = readSomething(queue);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    JValue _tree;

    int currentReading = JValue.TYPE_START_STRUCTURE;

    public boolean checkQueue(LinkedList<JValue> queue, int type1, int type2, int type3, int type4) {
        if (queue.size() >= 4) {
            if (queue.get(0).getType() != type1 && type1 != JValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != JValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(2).getType() != type3 && type3 != JValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(3).getType() != type4 && type4 != JValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean checkQueue(LinkedList<JValue> queue, int type1, int type2, int type3) {
        if (queue.size() >= 3) {
            if (queue.get(0).getType() != type1 && type1 != JValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != JValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(2).getType() != type3 && type3 != JValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean checkQueue(LinkedList<JValue> queue, int type1, int type2) {
        if (queue.size() >= 2) {
            if (queue.get(0).getType() != type1 && type1 != JValue.TYPE_ANY) {
                return false;
            }
            if (queue.get(1).getType() != type2 && type2 != JValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean checkQueue(LinkedList<JValue> queue, int type1) {
        if (queue.size() >= 1) {
            if (queue.get(0).getType() != type1 && type1 != JValue.TYPE_ANY) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String first5ToText(LinkedList<JValue> queue) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < 5; ++i) {
            if (i >= queue.size()) {
                break;
            }
            if (i != 0) {
                str.append(", ");
            }
            if (queue.get(i)._name == null) {
                throw new IllegalStateException();
            }
            str.append(queue.get(i).getTypeText() + "(" + queue.get(i)._name + ")");
        }
        return str.toString();
    }

    public JValue enterList(LinkedList<JValue> queue) {
        JValue result = new JValue(null);
        boolean commaFin = false;

        while (true) {
            if (checkQueue(queue, JValue.TYPE_END_LIST)) {
                if (result._children == null) {
                    result._hasArray = true;
                }
                queue.removeFirst();
                if (commaFin) {
                    System.out.println("comma fin ******");
                    result.addToArray(new JValue(""));
                }
                break;
            } else if (checkQueue(queue, JValue.TYPE_ANY)) {
                JValue value = readSomething(queue);
                result.addToArray(value);
                commaFin = false;
                if (checkQueue(queue, JValue.TYPE_COMMA)) {
                    queue.removeFirst();
                    commaFin = true;
                    continue;
                }
                continue;
            } else if (queue.isEmpty()) {
                break;
            } else {
                System.out.println("can't read list " + first5ToText(queue));
            }
        }
        return result;
    }

    public JValue enterStructure(LinkedList<JValue> queue) {
        JValue result = new JValue(null);
        boolean commaFin = false;

        while (true) {
            if (checkQueue(queue, JValue.TYPE_END_STRUCTURE)) {
                if (result._children == null) {
                    result._hasSturucture = true;
                }
                queue.removeFirst();
                if (commaFin) {
                    result.addToStructure(new JValue(null));
                }
                break;
            } else if (checkQueue(queue, JValue.TYPE_TEXT, JValue.TYPE_DOUBLECOMMA, JValue.TYPE_ANY)) {
                JValue name2 = queue.removeFirst();
                JValue dq = queue.removeFirst();
                JValue value = readSomething(queue);
                result.addToStructure(name2);
                name2.addToValue(value);
                commaFin = false;

                if (checkQueue(queue, JValue.TYPE_COMMA)) {
                    commaFin = true;
                    queue.removeFirst();
                    continue;
                }
            } else {
                throw new IllegalArgumentException("File format " + first5ToText(queue) + " file = " + _reader._file);
            }
        }
        return result;
    }

    public JValue readSomething(LinkedList<JValue> queue) {
        if (queue.isEmpty()) {
            return null;
        }

        JValue operand = queue.removeFirst();
        JValue result = null;
        switch (operand.getType()) {
            case JValue.TYPE_NULL:
                //System.out.println(operand._name +" -> reading null ... "+ first5ToText(queue));
                return operand;

            case JValue.TYPE_END_LIST:
            case JValue.TYPE_END_STRUCTURE:
                //System.out.println(operand._name +" -> reading bad ... "+ first5ToText(queue));
                break;

            case JValue.TYPE_TEXT:
                //System.out.println(operand._name +" -> reading text... "+ first5ToText(queue));
                return operand;

            case JValue.TYPE_COMMA:
            case JValue.TYPE_DOUBLECOMMA:
                //System.out.println(operand._name +" -> reading bad2 ... "+ first5ToText(queue));
                return operand;

            case JValue.TYPE_START_STRUCTURE:
                //System.out.println(operand._name +" -> reading structure ... "+ first5ToText(queue));
                JValue structure = enterStructure(queue);
                return structure;

            case JValue.TYPE_START_LIST:
                //System.out.println(operand._name +" -> reading list ... "+ first5ToText(queue));
                JValue list = enterList(queue);
                return list;

            default:
                break;
        }
        System.out.println("readed unknown " + first5ToText(queue));
        queue.removeFirst();
        return readSomething(queue);
    }

    JsonReader _reader;

    static int count = 20000;
    static Throwable _fatal = null;

    public static void doTest(File file) {
        System.out.println("*PARSE*" + file);
        JsonParser parser1;
        try {
            parser1 = new JsonParser(file);
        } catch (IOException ex) {
            _fatal = ex;
            return;
        }
        String form = parser1._tree.format();
        if (false) {
            System.out.println("*FORMAT*");        
            System.out.println(form);
        }
        //System.out.println("*DONE*");

        JsonParser parser2 = new JsonParser();
        try {
            parser2.parse(form);

            String form2 = parser2._tree.format();
            if (form.equals(form2)) {
                //System.out.println("Test O");
            } else {
                System.out.println("Test X");
                _fatal = new Throwable("Test X");
            }
        } catch (Throwable ex) {
            _fatal = ex;
            ex.printStackTrace();
        }
        if (_fatal == null && false) {
            for (int i = 0; i < parser1._parse1.size(); ++i) {
                String t1 = i >= parser1._parse1.size() ? null : parser1._parse1.get(i);
                String t2 = i >= parser2._parse1.size() ? null : parser2._parse1.get(i);
                if (t1 == t2) {
                    continue;
                } else if (t1 == null || t2 == null) {

                } else if (t1.equals(t2)) {
                    continue;
                }
                System.out.println(parser1._reader._file + "  -> index " + i + " / " + parser1._parse1.size());
                System.out.println("t1 = " + t1);
                System.out.println("t2 = " + t2);
            }
            if (parser1._parse1.size() != parser2._parse1.size()) {
                System.out.println("t1 = " + parser1._parse1);
                System.out.println("t1 = " + parser2._parse1);
            }
            else {
                System.out.println("t1 = t2");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        doTest(new File("C:\\Users\\yaman\\test.json"));
        recursiveCheck(new File("C:\\Users\\yaman\\.nuget\\packages\\microsoft.netcore.platforms\\2.0.0"));
        recursiveCheck(new File("C:\\Users\\All Users\\Microsoft"));
        if (false) {
            recursiveCheck(new File("C:\\Users"));
        }
    }

    public static void recursiveCheck(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File seek : children) {
                    recursiveCheck(seek);
                }
            }
            return;
        }
        if (file.getName().toLowerCase().endsWith(".json")) {
            try {
                if (count == 0) {
                    return;
                }
                if (_fatal != null) {
                    return;
                }
                doTest(file);
                if (--count == 0) {
                    return;
                }
            } catch (Throwable ex) {
                ex.printStackTrace();;
            }
        }
    }

}
