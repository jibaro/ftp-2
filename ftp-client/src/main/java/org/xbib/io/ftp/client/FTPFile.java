package org.xbib.io.ftp.client;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * The FTPFile class is used to represent information about files stored
 * on an FTP server.
 *
 * @see FTPFileEntryParser
 * @see FTPClient#listFiles
 **/
public class FTPFile implements Serializable {
    /**
     * A constant indicating an FTPFile is a file.
     ***/
    public static final int FILE_TYPE = 0;
    /**
     * A constant indicating an FTPFile is a directory.
     ***/
    public static final int DIRECTORY_TYPE = 1;
    /**
     * A constant indicating an FTPFile is a symbolic link.
     ***/
    public static final int SYMBOLIC_LINK_TYPE = 2;
    /**
     * A constant indicating an FTPFile is of unknown type.
     ***/
    public static final int UNKNOWN_TYPE = 3;
    /**
     * A constant indicating user access permissions.
     ***/
    public static final int USER_ACCESS = 0;
    /**
     * A constant indicating group access permissions.
     ***/
    public static final int GROUP_ACCESS = 1;
    /**
     * A constant indicating world access permissions.
     ***/
    public static final int WORLD_ACCESS = 2;
    /**
     * A constant indicating file/directory read permission.
     ***/
    public static final int READ_PERMISSION = 0;
    /**
     * A constant indicating file/directory write permission.
     ***/
    public static final int WRITE_PERMISSION = 1;
    /**
     * A constant indicating file execute permission or directory listing
     * permission.
     ***/
    public static final int EXECUTE_PERMISSION = 2;
    private static final long serialVersionUID = 9010790363003271996L;
    // If this is null, then list entry parsing failed
    private final boolean[][] permissions; // e.g. _permissions[USER_ACCESS][READ_PERMISSION]

    private int type;

    private int hardLinkCount;

    private long size;

    private String rawListing;

    private String user;

    private String group;

    private String name;

    private String link;

    private ZonedDateTime zonedDateTime;

    /*** Creates an empty FTPFile. ***/
    public FTPFile() {
        permissions = new boolean[3][3];
        type = UNKNOWN_TYPE;
        // init these to values that do not occur in listings
        // so can distinguish which fields are unset
        hardLinkCount = 0; // 0 is invalid as a link count
        size = -1; // 0 is valid, so use -1
        user = "";
        group = "";
        zonedDateTime = null;
        name = null;
    }

    /**
     * Constructor for use by {@link FTPListParseEngine} only.
     * Used to create FTPFile entries for failed parses
     *
     * @param rawListing line that could not be parsed.
     */
    FTPFile(String rawListing) {
        permissions = null; // flag that entry is invalid
        this.rawListing = rawListing;
        type = UNKNOWN_TYPE;
        // init these to values that do not occur in listings
        // so can distinguish which fields are unset
        hardLinkCount = 0; // 0 is invalid as a link count
        size = -1; // 0 is valid, so use -1
        user = "";
        group = "";
        zonedDateTime = null;
        name = null;
    }

    /***
     * Get the original FTP server raw listing used to initialize the FTPFile.
     *
     * @return The original FTP server raw listing used to initialize the
     *         FTPFile.
     ***/
    public String getRawListing() {
        return rawListing;
    }

    /***
     * Set the original FTP server raw listing from which the FTPFile was
     * created.
     *
     * @param rawListing  The raw FTP server listing.
     ***/
    public void setRawListing(String rawListing) {
        this.rawListing = rawListing;
    }

    /***
     * Determine if the file is a directory.
     *
     * @return True if the file is of type <code>DIRECTORY_TYPE</code>, false if
     *         not.
     ***/
    public boolean isDirectory() {
        return (type == DIRECTORY_TYPE);
    }

    /***
     * Determine if the file is a regular file.
     *
     * @return True if the file is of type <code>FILE_TYPE</code>, false if
     *         not.
     ***/
    public boolean isFile() {
        return (type == FILE_TYPE);
    }

    /***
     * Determine if the file is a symbolic link.
     *
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isSymbolicLink() {
        return (type == SYMBOLIC_LINK_TYPE);
    }

    /***
     * Determine if the type of the file is unknown.
     *
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isUnknown() {
        return (type == UNKNOWN_TYPE);
    }

    /**
     * Used to indicate whether an entry is valid or not.
     * If the entry is invalid, only the {@link #getRawListing()} method will be useful.
     * Other methods may fail.
     * <p>
     * Used in conjunction with list parsing that preseverves entries that failed to parse.
     *
     * @return true if the entry is valid
     * @see FTPClientConfig#setUnparseableEntries(boolean)
     */
    public boolean isValid() {
        return (permissions != null);
    }

    /***
     * Return the type of the file (one of the <code>_TYPE</code> constants),
     * e.g., if it is a directory, a regular file, or a symbolic link.
     *
     * @return The type of the file.
     ***/
    public int getType() {
        return type;
    }

    /***
     * Set the type of the file (<code>DIRECTORY_TYPE</code>,
     * <code>FILE_TYPE</code>, etc.).
     *
     * @param type  The integer code representing the type of the file.
     ***/
    public void setType(int type) {
        this.type = type;
    }

    /***
     * Return the name of the file.
     *
     * @return The name of the file.
     ***/
    public String getName() {
        return name;
    }

    /***
     * Set the name of the file.
     *
     * @param name  The name of the file.
     ***/
    public void setName(String name) {
        this.name = name;
    }

    /***
     * Return the file size in bytes.
     *
     * @return The file size in bytes.
     ***/
    public long getSize() {
        return size;
    }

    /**
     * Set the file size in bytes.
     *
     * @param size The file size in bytes.
     */
    public void setSize(long size) {
        this.size = size;
    }

    /***
     * Return the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     *
     * @return The number of hard links to this file.
     ***/
    public int getHardLinkCount() {
        return hardLinkCount;
    }

    /***
     * Set the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     *
     * @param links  The number of hard links to this file.
     ***/
    public void setHardLinkCount(int links) {
        hardLinkCount = links;
    }

    /***
     * Returns the name of the group owning the file.  Sometimes this will be
     * a string representation of the group number.
     *
     * @return The name of the group owning the file.
     ***/
    public String getGroup() {
        return group;
    }

    /***
     * Set the name of the group owning the file.  This may be
     * a string representation of the group number.
     *
     * @param group The name of the group owning the file.
     ***/
    public void setGroup(String group) {
        this.group = group;
    }

    /***
     * Returns the name of the user owning the file.  Sometimes this will be
     * a string representation of the user number.
     *
     * @return The name of the user owning the file.
     ***/
    public String getUser() {
        return user;
    }

    /***
     * Set the name of the user owning the file.  This may be
     * a string representation of the user number;
     *
     * @param user The name of the user owning the file.
     ***/
    public void setUser(String user) {
        this.user = user;
    }

    /***
     * If the FTPFile is a symbolic link, this method returns the name of the
     * file being pointed to by the symbolic link.  Otherwise it returns null.
     *
     * @return The file pointed to by the symbolic link (null if the FTPFile
     *         is not a symbolic link).
     ***/
    public String getLink() {
        return link;
    }

    /***
     * If the FTPFile is a symbolic link, use this method to set the name of the
     * file being pointed to by the symbolic link.
     *
     * @param link  The file pointed to by the symbolic link.
     ***/
    public void setLink(String link) {
        this.link = link;
    }

    /***
     * Returns the file timestamp.  This usually the last modification time.
     *
     * @return A Calendar instance representing the file timestamp.
     ***/
    public ZonedDateTime getTimestamp() {
        return zonedDateTime;
    }

    /***
     * Set the file timestamp.  This usually the last modification time.
     * The parameter is not cloned, so do not alter its value after calling
     * this method.
     *
     * @param zonedDateTime A Calendar instance representing the file timestamp.
     ***/
    public void setTimestamp(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    /***
     * Set if the given access group (one of the <code> _ACCESS </code>
     * constants) has the given access permission (one of the
     * <code> _PERMISSION </code> constants) to the file.
     *
     * @param access The access group (one of the <code> _ACCESS </code>
     *               constants)
     * @param permission The access permission (one of the
     *               <code> _PERMISSION </code> constants)
     * @param value  True if permission is allowed, false if not.
     * @throws ArrayIndexOutOfBoundsException if either of the parameters is out of range
     ***/
    public void setPermission(int access, int permission, boolean value) {
        permissions[access][permission] = value;
    }


    /***
     * Determines if the given access group (one of the <code> _ACCESS </code>
     * constants) has the given access permission (one of the
     * <code> _PERMISSION </code> constants) to the file.
     *
     * @param access The access group (one of the <code> _ACCESS </code>
     *               constants)
     * @param permission The access permission (one of the
     *               <code> _PERMISSION </code> constants)
     * @throws ArrayIndexOutOfBoundsException if either of the parameters is out of range
     * @return true if {@link #isValid()} is {@code true &&} the associated permission is set;
     * {@code false} otherwise.
     ***/
    public boolean hasPermission(int access, int permission) {
        if (permissions == null) {
            return false;
        }
        return permissions[access][permission];
    }

    /***
     * Returns a string representation of the FTPFile information.
     *
     * @return A string representation of the FTPFile information.
     */
    @Override
    public String toString() {
        return getRawListing();
    }


    private char formatType() {
        switch (type) {
            case FILE_TYPE:
                return '-';
            case DIRECTORY_TYPE:
                return 'd';
            case SYMBOLIC_LINK_TYPE:
                return 'l';
            default:
                return '?';
        }
    }

    private String permissionToString(int access) {
        StringBuilder sb = new StringBuilder();
        if (hasPermission(access, READ_PERMISSION)) {
            sb.append('r');
        } else {
            sb.append('-');
        }
        if (hasPermission(access, WRITE_PERMISSION)) {
            sb.append('w');
        } else {
            sb.append('-');
        }
        if (hasPermission(access, EXECUTE_PERMISSION)) {
            sb.append('x');
        } else {
            sb.append('-');
        }
        return sb.toString();
    }
}
