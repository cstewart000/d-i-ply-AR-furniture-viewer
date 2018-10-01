package com.diply.viewer;

public enum ModelsEnum {

    LEAN_AH ("Lean-ah shoe rack", R.raw.lean_ah_cen_hq, "https://d-i-ply.com/products/shoe-rack"),
    ON_AGAIN ("On-again-off-again Shelves",   R.raw.on_again_off_again_, "https://d-i-ply.com/products/shelving-unit"),
    RORY ("The Rory Record Cabinet", R.raw.the_rory, "https://d-i-ply.com/products/the-rory-shelving-unit"),
    RISER ("Glen's Screen Riser", R.raw.glens_monitor_riser_2_tier, "https://d-i-ply.com/products/glenns-screen-riser");

    private final String name;
    //private final int price;
    private final int modelResourceId;
    private final String urlToStore;


    ModelsEnum(String name, int modelResourceId, String urlToStore) {
        this.name = name;
        this.modelResourceId = modelResourceId;
        this.urlToStore = urlToStore;

    }

    public String getName() {
        return name;
    }

    public int getModelResourceId() {
        return modelResourceId;
    }

    public String getUrlToStore() {
        return urlToStore;
    }
}
