package com.database.auction.entity;

import com.database.auction.enums.Category;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "AuctionItems")
public class AuctionItems {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "auction_id", unique = true)
    private int auction_id;

    @Column(name = "seller_id")
    private int seller_id;

    @Column(name = "item_name")
    private String item_name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Column(name = "starting_price")
    private Double starting_price;

    @Column(name = "bid_increment")
    private Double bid_increment;

    @Column(name = "reserve_price")
    private Double reserve_price;

    @Column(name = "closing_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date closing_time;

    @Column(name = "description")
    private String description;

    // One auction item can have many images.
    @OneToMany(mappedBy = "auctionItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuctionImage> images = new ArrayList<>();

    public AuctionItems() {
    }

    // Getters and setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getauction_id() {
        return auction_id;
    }

    public void setauction_id(int auction_id) {
        this.auction_id = auction_id;
    }

    public int getseller_id() {
        return seller_id;
    }

    public void setseller_id(int seller_id) {
        this.seller_id = seller_id;
    }

    public String getitem_name() {
        return item_name;
    }

    public void setitem_name(String item_name) {
        this.item_name = item_name;
    }


    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getStartingPrice() {
        return starting_price;
    }

    public void setStartingPrice(Double starting_price) {
        this.starting_price = starting_price;
    }

    public Double getbid_increment() {
        return bid_increment;
    }

    public void setbid_increment(Double bid_increment) {
        this.bid_increment = bid_increment;
    }

    public Double getreserve_price() {
        return reserve_price;
    }

    public void setreserve_price(Double reserve_price) {
        this.reserve_price = reserve_price;
    }

    public Date getClosingTime() {
        return closing_time;
    }

    public void setClosingTime(Date closing_time) {
        this.closing_time = closing_time;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public List<AuctionImage> getImages() {
        return images;
    }

    public void setImages(List<AuctionImage> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "AuctionItems{" +
                "id=" + id +
                ", auction_id=" + auction_id +
                ", seller_id=" + seller_id +
                ", item_name='" + item_name + '\'' +
                ", category=" + category +
                ", startingPrice=" + starting_price +
                ", bid_increment=" + bid_increment +
                ", reserve_price=" + reserve_price +
                ", closingTime=" + closing_time +
                ", description='" + description + '\'' +
                ", images=" + images +
                '}';
    }
}
