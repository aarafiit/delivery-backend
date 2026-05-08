package kn.org.deliverybackend.enumeration;

public enum StorageFolder {
    PROFILES("profiles"),
    PRODUCTS("products"),
    BANNERS("banners"),
    RIDER_LICENSES("riders/licenses"),
    RIDER_NIDS("riders/nids"),
    SHOPS("shops");

    private final String path;

    StorageFolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
