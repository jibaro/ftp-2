package org.xbib.io.ftp.fs;


import org.xbib.io.ftp.client.FTP;
import org.xbib.io.ftp.client.FTPClient;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.OpenOption;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents FTP file types.
 */
public final class FileType implements OpenOption, CopyOption {

    private static final FileType ASCII_NO_FORMAT = new FileType(FTP.ASCII_FILE_TYPE, "ascii");
    private static final Map<Format, FileType> ASCII_WITH_FORMATS = getFileTypesWithFormats(FTP.ASCII_FILE_TYPE, "ascii");

    private static final FileType EBCDIC_NO_FORMAT = new FileType(FTP.EBCDIC_FILE_TYPE, "ebcdic");
    private static final Map<Format, FileType> EBCDIC_WITH_FORMATS = getFileTypesWithFormats(FTP.EBCDIC_FILE_TYPE, "ebcdic");

    private static final FileType BINARY_NO_FORMAT = new FileType(FTP.BINARY_FILE_TYPE, "binary");

    private static final FileType LOCAL_NO_BYTE_SIZE = new FileType(FTP.LOCAL_FILE_TYPE, "local");

    private static final int NO_FORMAT_OR_BYTE_SIZE = Integer.MIN_VALUE;
    private final int fileType;
    private final String fileTypeString;
    private final Format format;
    private final int formatOrByteSize;
    private FileType(int fileType, String fileTypeString) {
        this.fileType = fileType;
        this.fileTypeString = fileTypeString;
        this.formatOrByteSize = NO_FORMAT_OR_BYTE_SIZE;
        this.format = null;
    }

    private FileType(int fileType, String fileTypeString, Format format) {
        this.fileType = fileType;
        this.fileTypeString = fileTypeString;
        this.formatOrByteSize = format.format;
        this.format = format;
    }

    private FileType(int fileType, String fileTypeString, int byteSize) {
        this.fileType = fileType;
        this.fileTypeString = fileTypeString;
        this.formatOrByteSize = byteSize;
        this.format = null;
    }

    private static Map<Format, FileType> getFileTypesWithFormats(int fileType, String fileTypeString) {
        Map<Format, FileType> fileTypes = new EnumMap<>(Format.class);
        for (Format format : Format.values()) {
            fileTypes.put(format, new FileType(fileType, fileTypeString, format));
        }
        return Collections.unmodifiableMap(fileTypes);
    }

    /**
     * Returns an ASCII file type with an unspecified text format.
     *
     * @return An ASCII file type with an unspecified text format.
     */
    public static FileType ascii() {
        return ASCII_NO_FORMAT;
    }

    /**
     * Returns an ASCII file type with a specific text format.
     *
     * @param format The text format for the file type; ignored if {@code null}.
     * @return An ASCII file type with the given text format.
     */
    public static FileType ascii(Format format) {
        return format == null ? ASCII_NO_FORMAT : ASCII_WITH_FORMATS.get(format);
    }

    /**
     * Returns an EBCDIC file type with an unspecified text format.
     *
     * @return An EBCDIC file type with an unspecified text format.
     */
    public static FileType ebcdic() {
        return EBCDIC_NO_FORMAT;
    }

    /**
     * Returns an EBCDIC file type with a specific text format.
     *
     * @param format The text format for the file type; ignored if {@code null}.
     * @return An EBCDIC file type with the given text format.
     */
    public static FileType ebcdic(Format format) {
        return format == null ? EBCDIC_NO_FORMAT : EBCDIC_WITH_FORMATS.get(format);
    }

    /**
     * Returns a binary file type with an unspecified text format.
     *
     * @return A binary file type with an unspecified text format.
     */
    public static FileType binary() {
        return BINARY_NO_FORMAT;
    }

    /**
     * Returns a local file type with an unspecified byte size.
     *
     * @return A local file type with an unspecified byte size.
     */
    public static FileType local() {
        return LOCAL_NO_BYTE_SIZE;
    }

    /**
     * Returns a local file type with a specific byte size.
     *
     * @param byteSize The byte size for the file type; ignored if not larger than 0.
     * @return A binary file type with the given text format.
     */
    public static FileType local(int byteSize) {
        return byteSize <= 0 ? LOCAL_NO_BYTE_SIZE : new FileType(FTP.LOCAL_FILE_TYPE, "local", byteSize);
    }

    void apply(FTPClient client) throws IOException {
        boolean result = formatOrByteSize == NO_FORMAT_OR_BYTE_SIZE ? client.setFileType(fileType) : client.setFileType(fileType, formatOrByteSize);
        if (!result) {
            throw new FTPFileSystemException(client.getReplyCode(), client.getReplyString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        FileType other = (FileType) o;
        return fileType == other.fileType
                && formatOrByteSize == other.formatOrByteSize;
    }

    @Override
    public int hashCode() {
        int hash = fileType;
        hash = 31 * hash + formatOrByteSize;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(getClass().getSimpleName())
                .append('.')
                .append(fileTypeString);
        if (format != null) {
            sb.append('(')
                    .append(format)
                    .append(')');
        } else if (formatOrByteSize != NO_FORMAT_OR_BYTE_SIZE) {
            sb.append('(')
                    .append(formatOrByteSize)
                    .append(')');
        }
        return sb.toString();
    }

    /**
     * The possible FTP text formats.
     */
    public enum Format {
        /**
         * Indicates a non-print text format.
         */
        NON_PRINT(FTP.NON_PRINT_TEXT_FORMAT),
        /**
         * Indicates that text files contain format vertical format control characters.
         */
        TELNET(FTP.TELNET_TEXT_FORMAT),
        /**
         * Indicates that text files contain ASA vertical format control characters.
         */
        CARRIAGE_CONTROL(FTP.CARRIAGE_CONTROL_TEXT_FORMAT),;

        private final int format;

        Format(int format) {
            this.format = format;
        }
    }
}
