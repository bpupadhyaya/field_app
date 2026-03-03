package com.fieldapp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/site")
public class SiteController {
    @GetMapping("/navigation")
    public Object nav() {
        return List.of(
                Map.of("key", "equipment", "title", "Equipment", "items", List.of("All Products", "Lawn & Garden", "Electric", "Agriculture", "Construction", "Landscaping & Grounds Care", "Golf & Sports Turf", "Forestry & Logging", "Engines & Drivetrain", "Electronics", "Government & Military Sales", "Attachments, Accessories, & Implements", "Technology Products", "Rental Sales", "View Used Equipment")),
                Map.of("key", "finance", "title", "Finance", "items", List.of("Offers & Discounts", "Make a Payment", "Manage My Account", "Our Company Financial", "Agriculture", "Construction", "Landscaping & Grounds Care", "Lawn & Garden", "Golf & Sports Turf", "Forestry & Logging")),
                Map.of("key", "parts", "title", "Parts & Service", "items", List.of("Parts", "Run It Your Way", "Buy Parts", "Manuals & Training", "Owner Support", "Warranty, Service & Protection Plans", "StellarSupport", "Recalls", "Safety", "Self-Repair")),
                Map.of("key", "digital", "title", "Digital", "items", List.of("Digital Tools")),
                Map.of("key", "company", "title", "Our Company & Purpose", "items", List.of("Our Company & Purpose", "U.S. Impact", "Locations", "Careers", "Explore Our Company", "News", "Technology & Innovation", "Investor Relations"))
        );
    }
}
