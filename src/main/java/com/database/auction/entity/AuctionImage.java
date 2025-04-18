package com.database.auction.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "AuctionImages")
public class AuctionImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id")
    private AuctionItems auctionItem;

    public AuctionImage() {
    }

    public AuctionImage(String imageUrl, AuctionItems auctionItem) {
        this.imageUrl = imageUrl;
        this.auctionItem = auctionItem;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public AuctionItems getAuctionItem() {
        return auctionItem;
    }

    public void setAuctionItem(AuctionItems auctionItem) {
        this.auctionItem = auctionItem;
    }
}
