package br.edu.udesc.reservaservice.shared.util;

public final class CpfUtils {

    private CpfUtils() {
    }

    public static String normalizar(String cpf) {
        if (cpf == null) {
            return null;
        }

        return cpf.replaceAll("\\D", "");
    }

    public static boolean formatoValido(String cpf) {
        String normalizado = normalizar(cpf);
        return normalizado != null && normalizado.matches("\\d{11}");
    }
}
