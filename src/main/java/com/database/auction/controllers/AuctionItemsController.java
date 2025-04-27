package com.database.auction.controllers;


import com.database.auction.dto.AuctionItemDto;
import com.database.auction.dto.AuctionItemSellerSummaryDto;
import com.database.auction.dto.AuctionItemSummaryDto;
import com.database.auction.dto.QuestionDTO;
import com.database.auction.entity.AuctionImage;
import com.database.auction.entity.AuctionItems;
import com.database.auction.enums.Category;
import com.database.auction.exception.AuctionItemNotFoundException;
import com.database.auction.mapper.AuctionItemsMapper;
import com.database.auction.repository.AuctionImageRepository;
import com.database.auction.repository.AuctionItemsRepository;
import com.database.auction.service.AuctionItemsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RestController

@RequestMapping("/auth/auction-items")
public class AuctionItemsController {

    private final AuctionItemsService auctionItemsService;
    private AuctionItemsRepository auctionItemsRepository;
    private AuctionItemsMapper auctionItemsMapper;
    private AuctionImageRepository imagesRepo;

    @Autowired
    public AuctionItemsController(AuctionItemsService auctionItemsService,AuctionItemsRepository itemsRepo,
                                  AuctionImageRepository imagesRepo) {
        this.auctionItemsService = auctionItemsService;
        this.auctionItemsRepository = itemsRepo;
        this.imagesRepo = imagesRepo;
    }

    // Endpoint to retrieve all auction items
    @GetMapping("/all")
    public List<AuctionItemDto> getAllAuctionItems() {
        System.out.println("All items will be displayed now");
        return auctionItemsService.findAllAuctionItems();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<AuctionItemSummaryDto>> getAuctionItemSummaries() {
        log.info("We have received the calling from frontend");
        return ResponseEntity.ok(auctionItemsService.findAllAuctionItemSummaries());
    }

    @GetMapping("/summary/{category}")
    public ResponseEntity<List<AuctionItemSummaryDto>> getByCategory(
            @PathVariable String category) {
        Category cat;
        try {
            cat = Category.valueOf(category.toLowerCase());
        } catch (IllegalArgumentException ex) {
            // not one of CAR, BIKE, TRUCK
            return ResponseEntity
                    .badRequest()
                    .build();
        }
        List<AuctionItemSummaryDto> dtos =
                auctionItemsService.findAuctionItemsByCategory(cat);
        return ResponseEntity.ok(dtos);
    }

    // NEW: lookup by auctionId, return as ResponseEntity
    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionItemDto> getAuctionItemByAuctionId(
            @PathVariable int auctionId) {

        AuctionItemDto dto = auctionItemsService.findAuctionItemByAuctionId(auctionId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(
            value = "/{seller_id}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadAuctionItem(
          //  @RequestParam int auctionId,
          @PathVariable("seller_id") int sellerId,
          @RequestParam("item_name") String itemName,
          @RequestParam("category") Category category,
          @RequestParam("starting_price") Double startingPrice,
          @RequestParam("bid_increment") Double bidIncrement,
          @RequestParam("closing_time")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime closingTime,
          @RequestParam("start_time")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startTime,
          @RequestParam(value = "description", required = false) String description,
          @RequestParam("min_price")       Double minPrice,
          @RequestParam(value = "current_bid", required = false) Double currentBid,
          @RequestParam("images") MultipartFile[] images
    ) throws IOException {
        log.info("inserting the images");
        // Convert LocalDateTime to java.util.Date
        Date closing = Date.from(closingTime.atZone(ZoneId.systemDefault()).toInstant());

        // 1) Save AuctionItems
        AuctionItems item = new AuctionItems();
       // item.setauction_id(auctionId);
        item.setseller_id(sellerId);
        item.setitem_name(itemName);
        item.setCategory(category);
        item.setStartingPrice(startingPrice);
        item.setbid_increment(bidIncrement);
        item.setStartTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
        item.setClosingTime(closing);
        item.setDescription(description);
        item.setCurrentBid(currentBid != null ? currentBid : startingPrice);
        item.setMinPrice(minPrice);

        // ────────────────────────────────────────────────────────────
        // ★ Here’s the new bit: manually assign the primary key ★
        Long maxId = auctionItemsRepository.findMaxId();      // calls your @Query MAX(id)
        item.setId(maxId + 1);                   // next sequential id
        // ────────────────────────────────────────────────────────────
        AuctionItems saved = auctionItemsRepository.save(item);


        // 2) Save uploaded images
        for (MultipartFile file : images) {
            AuctionImage img = new AuctionImage();
            img.setImageUrl(file.getOriginalFilename());
            img.setAuctionItem(saved);
            img.setImageData(file.getBytes());
            imagesRepo.save(img);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{itemId}/images/{imageId}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long itemId,
            @PathVariable Long imageId) {

        Optional<AuctionImage> opt = imagesRepo.findById(imageId);
        if (opt.isEmpty() || !opt.get().getAuctionItem().getId().equals(itemId)) {
            return ResponseEntity.notFound().build();
        }

        AuctionImage img = opt.get();
        // Determine MIME type if you stored it, otherwise default to JPEG
        MediaType mediaType = MediaType.IMAGE_JPEG;
        // If file name ends with .png, you could switch:
        if (img.getImageUrl().toLowerCase().endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(img.getImageData());
    }

    /** Seller summary: highest-reserve buyer per auction */
    @GetMapping("/summarySeller/{sellerId}")
    public ResponseEntity<List<AuctionItemSellerSummaryDto>> getSellerSummary(
            @PathVariable int sellerId) {
        List<AuctionItemSellerSummaryDto> list = auctionItemsService.findSellerSummary(sellerId);
        return ResponseEntity.ok(list);
    }

    @PostMapping(value="/update_answer/{question_id}/{auction_id}",consumes=MediaType.APPLICATION_JSON_VALUE)
    public String updateanswer(
            @PathVariable int question_id,
            @PathVariable int auction_id,
		    @RequestBody QuestionDTO quesdto)
    {
        System.out.println("In Controller");

        return auctionItemsService.updateanswer(question_id,auction_id,quesdto.getAnswer());

    }

    @PutMapping(value="/insertquestion/{auction_id}",consumes=MediaType.APPLICATION_JSON_VALUE)
    public String insertquestion(

            @PathVariable int auction_id,
            @RequestBody QuestionDTO quesdto)
    {
        System.out.println("In Controller");

        return auctionItemsService.insertquestion(auction_id,quesdto.getQuestion());

    }

    @GetMapping(
            value = "/getallquessans/{auction_id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<QuestionDTO>> getAllQuesAndAns(
            @PathVariable("auction_id") int auction_id) {
        System.out.println("In Controller");
        log.info("auction_id: {}", auction_id);

        List<QuestionDTO> questions = auctionItemsService.getallquessans(auction_id);

        if (questions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/getSalesReportByAuctionId/{auction_id}")
    public List<AuctionItemDto> getSalesReportByAuctionId(
            @PathVariable Integer auction_id
            )
    {
        System.out.println("In Controller");
        log.info("auction_id"+auction_id );
        return auctionItemsService.getSalesReportByAuctionId(auction_id);
    }




}
