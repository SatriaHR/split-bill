package com.splitbill.web;

import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.splitbill.service.BillDetailService;
import com.splitbill.web.dto.Requests;
import com.splitbill.web.dto.Responses;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bill-details")
public class BillDetailController {
    private final BillDetailService details;
    public BillDetailController(BillDetailService details) { this.details = details; }
    @PostMapping("/for/{headId}") public ResponseEntity<Responses.ApiResponse<Responses.BillDetail>> create(@PathVariable Long headId, @Valid @RequestBody Requests.BillDetailCreate r) { return ResponseHandler.response(HttpStatus.CREATED, "Bill detail created successfully", Responses.detail(details.create(headId, r))); }
    @GetMapping public ResponseEntity<?> all() { return ResponseHandler.response(HttpStatus.OK, "Bill details retrieved successfully", StreamSupport.stream(details.all().spliterator(), false).map(Responses::detail).toList()); }
    @GetMapping("/{id}") public ResponseEntity<Responses.ApiResponse<Responses.BillDetail>> get(@PathVariable Long id) { return ResponseHandler.response(HttpStatus.OK, "Bill detail retrieved successfully", Responses.detail(details.get(id))); }
    @PutMapping("/{id}") public ResponseEntity<Responses.ApiResponse<Responses.BillDetail>> update(@PathVariable Long id, @Valid @RequestBody Requests.BillDetailCreate r) { return ResponseHandler.response(HttpStatus.OK, "Bill detail updated successfully", Responses.detail(details.update(id, r))); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { details.delete(id); }
}
