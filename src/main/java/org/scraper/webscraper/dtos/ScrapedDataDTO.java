package org.scraper.webscraper.dtos;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * A record for storing the minimum required price point data scraped from live pages. <strong> NOTE: </strong>
 * ScrapedDataDTOs should be converted into GenericPricePointDTOs with the additional fields - i.e. vendor, currency,
 * scrapedAt etc. injected later.
 */
@Builder
public record ScrapedDataDTO(String modelNumber, BigDecimal price) {}