package pt.ipleiria.estg.dei.ei.dae.academics.utils;

import jakarta.ws.rs.core.MultivaluedMap;

public class PublicationUtils {

    private PublicationUtils() {}

    public static String getFileName(MultivaluedMap<String, String> headers) {
        String[] contentDisposition = headers.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "unknown";
    }
}
