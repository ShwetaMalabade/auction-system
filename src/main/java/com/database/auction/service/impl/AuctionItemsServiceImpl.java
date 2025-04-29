package com.database.auction.service.impl;


import com.database.auction.dto.*;
import com.database.auction.entity.AuctionImage;
import com.database.auction.entity.AuctionItems;
import com.database.auction.entity.Bid;
import com.database.auction.entity.Users;
import com.database.auction.enums.Category;
import com.database.auction.enums.RoleType;
import com.database.auction.exception.AuctionItemNotFoundException;
import com.database.auction.mapper.AuctionItemsMapper;
import com.database.auction.mapper.QuestionRowMapper;
import com.database.auction.repository.AuctionItemsRepository;
import com.database.auction.repository.BidRepository;
import com.database.auction.repository.UsersRepository;
import com.database.auction.service.AuctionItemsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuctionItemsServiceImpl implements AuctionItemsService {

    private final AuctionItemsRepository auctionItemsRepository;
    private final AuctionItemsMapper auctionItemsMapper;
    private final UsersRepository userRepository;
    private final BidRepository bidRepo;
    private final JdbcTemplate jdbc;

    @Autowired
    public AuctionItemsServiceImpl(AuctionItemsRepository auctionItemsRepository,
                                   AuctionItemsMapper auctionItemsMapper, UsersRepository userRepository,
                                   BidRepository bidRepo,JdbcTemplate jdbc) {
        this.auctionItemsRepository = auctionItemsRepository;
        this.auctionItemsMapper = auctionItemsMapper;
        this.userRepository = userRepository;
        this.bidRepo = bidRepo;
        this.jdbc = jdbc;
    }

    @Override
    public List<AuctionItemDto> findAllAuctionItems() {
        List<AuctionItems> items = auctionItemsRepository.findAll();
        return items.stream()
                .map(auctionItemsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuctionItemSummaryDto> findAllAuctionItemSummaries() {
        List<AuctionItems> items = auctionItemsRepository.findAll();
        log.info(String.valueOf(items.stream()
                    .map(auctionItemsMapper::toSummaryDto).toList()));
        return items.stream()
                .map(auctionItemsMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuctionItemDto insertAuctionItem(AuctionItemDto auctionItemDto) {
        log.info("Inserting new Auction Item");
        // Verify the seller exists and is of role SELLER
        Users seller = userRepository.findByUserId(auctionItemDto.getSellerId());
        if (seller == null || !RoleType.SELLER.equals(seller.getRole())) {
            throw new RuntimeException("Invalid seller: User not found or does not have SELLER role");
        }

        // Map the DTO to an AuctionItems entity
        AuctionItems auctionItem = auctionItemsMapper.toEntity(auctionItemDto);

        // If image URLs are provided in DTO, create AuctionImage entities
        if (auctionItemDto.getImages() != null && !auctionItemDto.getImages().isEmpty()) {
            List<AuctionImage> images = auctionItemDto.getImages().stream()
                    .map(url -> {
                        AuctionImage img = new AuctionImage();
                        img.setImageUrl(url);
                        img.setAuctionItem(auctionItem);
                        return img;
                    }).collect(Collectors.toList());
            auctionItem.setImages(images);
        }

        // initialize currentBid
        if (auctionItemDto.getCurrentBid() != null) {
            auctionItem.setCurrentBid(auctionItemDto.getCurrentBid());
        } else {
            auctionItem.setCurrentBid(auctionItem.getStartingPrice());
        }

        // Persist the auction item in the database
        AuctionItems savedItem = auctionItemsRepository.save(auctionItem);
        return auctionItemsMapper.toDto(savedItem);
    }

    @Override
    public AuctionItemDto findAuctionItemByAuctionId(int auction_id) {
        AuctionItems item = auctionItemsRepository.findByAuctionIdNative(auction_id)
                .orElseThrow(() -> new AuctionItemNotFoundException(
                        "Auction item not found with auctionId=" + auction_id));
        return auctionItemsMapper.toDto(item);
    }

    @Override
    public List<AuctionItemSummaryDto> findAuctionItemsByCategory(Category category) {
        List<AuctionItems> items = auctionItemsRepository.findAllByCategory(category);
        return items.stream()
                .map(auctionItemsMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuctionItemSellerSummaryDto> findSellerSummary(int sellerId) {
        return auctionItemsRepository.findBySellerId(sellerId).stream().map(item -> {
            AuctionItemSellerSummaryDto dto = new AuctionItemSellerSummaryDto();
            dto.setAuctionId(item.getauction_id());

            // build image URLs
            List<String> urls = item.getImages().stream()
                    .map(img -> "http://localhost:8080/auth/auction-items/"
                            + item.getId()
                            + "/images/"
                            + img.getId())
                    .collect(Collectors.toList());
            dto.setImages(urls);

            dto.setDescription(item.getDescription());
            dto.setItemName(item.getitem_name());
            dto.setCategory(item.getCategory());
            dto.setClosingTime(item.getClosingTime());
            dto.setCurrentBid(item.getCurrentBid());
            dto.setMinPrice(item.getMinPrice());
//            // find highest-reserve bidder
//            List<Bid> bids = bidRepo.findAllByAuctionItem_Id(item.getId());
//            dto.setBuyerUsername(
//                    bids.stream()
//                            .max(Comparator.comparing(Bid::getReservePrice))
//                            .map(b -> b.getBuyer().getUsername())
//                            .orElse(null)
//            );

            Integer winnerId = item.getWinningBuyerId();
            if (winnerId != null) {
                userRepository.findById(winnerId)
                        .ifPresent(u -> dto.setBuyerUsername(u.getUsername()));
            } else {
                dto.setBuyerUsername(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }
    public int updateanswer(int question_id,int auction_id,String answer) {
        System.out.println("In Service Implementation");
        // fetch the username for this userId

        // reuse your JDBC-based loader
        System.out.println(auction_id);
        String sql = """
        
            UPDATE auction_questions q
            SET  q.answer = ?
            WHERE q.question_id = ? and q.auction_id=?;
        """;

        int rows= jdbc.update(
                sql,
                answer,
                question_id,auction_id

        );
        System.out.println("Rows updated: " + rows);

        if (rows != 1) {

            throw new EntityNotFoundException("Question not found: " + question_id);
        }

        return rows;
    }

    public int insertquestion(int auctionId, String question) {
        System.out.println("In Service Implementation");
        System.out.println("Auction ID: " + auctionId);
        System.out.println("Question: " + question);

        String sql = """
        INSERT INTO auction_questions (auction_id, question)
        VALUES (?, ?)
        """;

        int rowsAffected = jdbc.update(sql, auctionId, question);
        System.out.println("Rows inserted: " + rowsAffected);

        if (rowsAffected != 1) {
            throw new IllegalStateException(
                    "Failed to insert question for auction ID " + auctionId);
        }

        return rowsAffected;
    }


    public List<QuestionDTO> getallquessans(int auctionId) {
        System.out.println("In Service Implementation");
        log.info("Fetching questions for auction_id: {}", auctionId);

        String sql = """
        SELECT *
          FROM auction_questions
         WHERE auction_id = ?
    """;

        List<QuestionDTO> list = jdbc.query(
                sql,
                new Object[]{ auctionId },
                (rs, rowNum) -> {
                    QuestionDTO q = new QuestionDTO();
                    q.setQuestionId( rs.getInt("question_id"));
                    q.setAuctionId(  rs.getInt("auction_id"));
                    q.setQuestion(   rs.getString("question"));
                    q.setAnswer(     rs.getString("answer"));
                    return q;
                }
        );

        // **Remove** the exception-throw here—let the controller handle empty lists
        // if (list.isEmpty()) {
        //     throw new EntityNotFoundException("No answered questions found for auction_id: " + auctionId);
        // }

        return list;
    }


    public List<AuctionItemDto> getSalesReportByAuctionId(Integer auctionId) {
        System.out.println("In Service Implementation");

        if (auctionId == null) {
            throw new IllegalArgumentException("auctionId must be provided");
        }

        String sql = """
        SELECT *
          FROM auction_items
         WHERE auction_id = ?
        """;

        List<AuctionItemDto> list = jdbc.query(
                sql,
                new Object[]{ auctionId },
                (rs, rowNum) -> {
                    AuctionItemDto p = new AuctionItemDto();
                    p.setItemName(      rs.getString("item_name"));
                    p.setStartingPrice( rs.getDouble("starting_price"));
                    p.setBidIncrement(  rs.getDouble("bid_increment"));
                    p.setSellerId(      rs.getInt("seller_id"));
                    String cat = rs.getString("category");
                    p.setCategory(cat == null
                            ? null
                            : Category.valueOf(cat));
                    p.setClosingTime(   rs.getDate("closing_time"));
                    p.setDescription(   rs.getString("description"));
                    p.setCurrentBid(    rs.getDouble("current_bid"));
                    return p;
                }
        );

        if (list.isEmpty()) {
            throw new EntityNotFoundException("No items found for auctionId=" + auctionId);
        }

        return list;
    }

    public List<AuctionItemDto> getsalesreport() {
        System.out.println("In Service Implementation");



        String sql = """
        SELECT *
          FROM auction_items where current_bid<>0
        """;

        List<AuctionItemDto> list = jdbc.query(
                sql,
                new Object[]{ },
                (rs, rowNum) -> {
                    AuctionItemDto p = new AuctionItemDto();
                    p.setItemName(      rs.getString("item_name"));
                    p.setStartingPrice( rs.getDouble("starting_price"));
                    p.setBidIncrement(  rs.getDouble("bid_increment"));
                    p.setSellerId(      rs.getInt("seller_id"));
                    String cat = rs.getString("category");
                    p.setCategory(cat == null
                            ? null
                            : Category.valueOf(cat));
                    p.setClosingTime(   rs.getDate("closing_time"));
                    p.setDescription(   rs.getString("description"));
                    p.setCurrentBid(    rs.getDouble("current_bid"));
                    return p;
                }
        );

        if (list.isEmpty()) {
            throw new EntityNotFoundException("No items found for");
        }

        return list;
    }


    public List<AuctionItemDto> getsalesreportByCategory(String category) {
        System.out.println("In Service Implementation");
        // fetch the questions for this itemId

        if (category == null) {
            throw new IllegalArgumentException("category must be provided");
        }

        String sql = """
                       Select * from auction_items where category=?;
                    """;

        List<AuctionItemDto> list = jdbc.query(
                sql,
                new Object[]{category},
                (rs, rowNum) -> {
                    AuctionItemDto p = new AuctionItemDto();
                    p.setItemName(rs.getString("item_name"));
                    p.setStartingPrice(rs.getDouble("starting_price"));
                    p.setBidIncrement(rs.getDouble("bid_increment"));
                    p.setSellerId(rs.getInt("seller_id"));
                    String cat = rs.getString("category");
                    p.setCategory(cat == null
                            ? null
                            : Category.valueOf(cat));
                    p.setClosingTime(rs.getDate("closing_time"));
                    p.setDescription(rs.getString("description"));
                    p.setCurrentBid(rs.getDouble("current_bid"));
                    return p;
                }
        );
        if (list.size() == 0) {

            throw new EntityNotFoundException("Item not found: " + category);
        }

        return list;
    }



    public List<AuctionItemDto> getsalesreportBySellerId(Integer seller_id) {
        System.out.println("In Service Implementation");
        // fetch the questions for this itemId
        Users seller = userRepository.findByUserId(seller_id);
        if(seller_id==null || !RoleType.SELLER.equals(seller.getRole())){
            throw new IllegalArgumentException("Seller id must be provided");

        }

        String sql = """
       Select * from auction_items where seller_id=?;
    """;

        List<AuctionItemDto> list = jdbc.query(
                sql,
                new Object[]{ seller_id },
                (rs, rowNum) -> {
                    AuctionItemDto p = new AuctionItemDto();
                    p.setItemName(      rs.getString("item_name"));
                    p.setStartingPrice( rs.getDouble("starting_price"));
                    p.setBidIncrement(  rs.getDouble("bid_increment"));
                    p.setSellerId(      rs.getInt("seller_id"));
                    String cat = rs.getString("category");
                    p.setCategory(cat == null
                            ? null
                            : Category.valueOf(cat));
                    p.setClosingTime(   rs.getDate("closing_time"));
                    p.setDescription(   rs.getString("description"));
                    p.setCurrentBid(    rs.getDouble("current_bid"));
                    return p;
                }
        );
        if (list.size()== 0) {

            throw new EntityNotFoundException("Item not found: " + seller_id);
        }

        return list;
    }

    @Override
    public List<BuyerOrderDTO> findOrdersByBuyer(int buyerId) {
        Date now = new Date();
        return auctionItemsRepository
                .findByWinningBuyerIdAndClosingTimeBefore(buyerId, now)
                .stream()
                .map(item -> {
                    BuyerOrderDTO dto = new BuyerOrderDTO();
                    dto.setAuctionId(item.getauction_id());

                    List<String> urls = item.getImages().stream()
                            .limit(1)
                            .map(img -> "http://localhost:8080/auth/auction-items/"
                                    + item.getId()
                                    + "/images/"
                                    + img.getId())
                            .collect(Collectors.toList());
                    dto.setImages(urls);

                    dto.setDescription(item.getDescription());
                    dto.setItemName(item.getitem_name());
                    dto.setCategory(item.getCategory());
                    dto.setCurrentBid(item.getCurrentBid());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getUnansweredQuestions(int auctionId) {
        String sql = """
        SELECT question_id,
               auction_id,
               question,
               answer
          FROM auction_questions
         WHERE auction_id = ?
           AND answer IS NULL
        """;

        List<QuestionDTO> list = jdbc.query(
                sql,
                new Object[]{ auctionId },
                (rs, rowNum) -> {
                    QuestionDTO q = new QuestionDTO();
                    q.setQuestionId(rs.getInt("question_id"));
                    q.setAuctionId( rs.getInt("auction_id"));
                    q.setQuestion(  rs.getString("question"));
                    // .getAnswer() will remain null
                    return q;
                }
        );

        return list;
    }

    @Override
    public List<AuctionItemDto> searchAuctions(String query) {
        // 1) text‐based matches
        List<AuctionItems> textMatches =
                auctionItemsRepository.findByItemNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        query, query);

        // 2) numeric match if query is a number
        List<AuctionItems> bidMatches = Collections.emptyList();
        try {
            Double bidVal = Double.valueOf(query);
            bidMatches = auctionItemsRepository.findByCurrentBid(bidVal);
        } catch (NumberFormatException e) {
            // not a number → ignore
        }

        // 3) union + dedupe
        Map<Long,AuctionItems> merged = new LinkedHashMap<>();
        for (AuctionItems a: textMatches) merged.put(a.getId(), a);
        for (AuctionItems a: bidMatches) merged.put(a.getId(), a);

        // 4) map to DTOs
        return merged.values().stream()
                .map(auctionItemsMapper::toDto)
                .collect(Collectors.toList());
    }








}
