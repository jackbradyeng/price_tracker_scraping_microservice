package org.scraper.webscraper.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A translation layer between a generic price point, and it's respective entity. <strong> NOTE: </strong>
 * GenericPricePointDTOs should be mapped to [Product]PricePoint entities using the MapperFactory abstraction.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenericPricePointDTO {

    private Long id;
    private String modelNumber;
    private String vendor;
    private String currency;
    private BigDecimal price;
    private LocalDateTime scrapedAt;
}