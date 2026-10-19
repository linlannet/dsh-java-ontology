package net.linlan.dsh.ontology.test;

public class H2Functions {

    public static int findInSet(String str, String strList) {
        if (str == null || strList == null || strList.isEmpty()) {
            return 0;
        }
        String[] parts = strList.split(",");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].trim().equals(str.trim())) {
                return i + 1;
            }
        }
        return 0;
    }
}
