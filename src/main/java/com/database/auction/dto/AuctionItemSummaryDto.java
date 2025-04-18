package com.database.auction.dto;

import com.database.auction.enums.Category;

import java.util.Date;
import java.util.List;

public class AuctionItemSummaryDto {

    private List<String> images;
    private String itemName;
    private Category category;
    private Double startingPrice;
    private Date closingTime;

    public AuctionItemSummaryDto() {
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getItemName() {
        return itemName;
    }
 
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
 

 
    public Double getStartingPrice() {
        return startingPrice;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setStartingPrice(Double startingPrice) {
        this.startingPrice = startingPrice;
    }
 
    public Date getClosingTime() {
        return closingTime;
    }
 
    public void setClosingTime(Date closingTime) {
        this.closingTime = closingTime;
    }
}
