package org.scraper.webscraper.constants;

public final class ScrapingConstants {

    // SCRAPING SLEEP CONSTANTS - AVOIDS OVERWHELMING TARGET SITES WITH TRAFFIC
    public static final Integer UMART_SLEEPING_CONSTANT = 500;
    // SCORPTEC SLEEPING CONSTANT IS SET HIGHER DUE TO RATE LIMITING CONCERNS
    public static final Integer SCORPTEC_SLEEPING_CONSTANT = 4000;

    // UMART CRON TIMESTAMPS
    public static final String UMART_GPU_SCRAPING_TIME = "0 00 22 * * ?";
    public static final String UMART_RAM_SCRAPING_TIME = "0 5 22 * * ?";
    public static final String UMART_CPU_SCRAPING_TIME = "0 10 22 * * ?";
    public static final String UMART_GPU_WORKSTATION_SCRAPING_TIME = "0 15 22 * * ?";
    public static final String UMART_HDD_SCRAPING_TIME = "0 20 22 * * ?";
    public static final String UMART_SSD_SCRAPING_TIME = "0 25 22 * * ?";
    public static final String UMART_NVME_SCRAPING_TIME = "0 30 22 * * ?";

    // SCORPTEC CRON TIMESTAMPS
    public static final String SCORPTEC_GPU_SCRAPING_TIME = "0 00 20 * * ?";
    public static final String SCORPTEC_RAM_SCRAPING_TIME = "0 10 20 * * ?";
    public static final String SCORPTEC_CPU_SCRAPING_TIME = "0 20 20 * * ?";
    public static final String SCORPTEC_GPU_WORKSTATION_SCRAPING_TIME = "0 30 20 * * ?";
    public static final String SCORPTEC_HDD_SCRAPING_TIME = "0 40 20 * * ?";
    public static final String SCORPTEC_SSD_SCRAPING_TIME = "0 50 20 * * ?";
    public static final String SCORPTEC_NVME_SCRAPING_TIME = "0 00 21 * * ?";
}