package com.database.auction.repository;


import com.database.auction.entity.AuctionItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionItemsRepository extends JpaRepository<AuctionItems, Long> {
    // Additional custom queries (if needed) go here
}
