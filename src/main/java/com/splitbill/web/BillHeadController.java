package com.splitbill.web;

import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.splitbill.domain.AppUser;
import com.splitbill.service.BillHeadService;
import com.splitbill.web.dto.Requests;
import com.splitbill.web.dto.Responses;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bill-heads")
public class BillHeadController {
    private final BillHeadService heads;
    public BillHeadController(BillHeadService heads) { this.heads = heads; }
    @PostMapping public ResponseEntity<Responses.ApiResponse<Responses.BillHead>> create(@Valid @RequestBody Requests.BillHeadCreate r, @AuthenticationPrincipal AppUser user) { 
        return ResponseHandler.response(HttpStatus.CREATED, "Bill head created successfully", Responses.head(heads.create(r, user.getId()))); 
    }
    @GetMapping public ResponseEntity<?> all() { return ResponseHandler.response(HttpStatus.OK, "Bill heads retrieved successfully", StreamSupport.stream(heads.all().spliterator(), false).map(Responses::head).toList()); }
    @GetMapping("/{id}") public ResponseEntity<Responses.ApiResponse<Responses.BillHead>> get(@PathVariable Long id) { return ResponseHandler.response(HttpStatus.OK, "Bill head retrieved successfully", Responses.head(heads.get(id))); }
    @PutMapping("/{id}") public ResponseEntity<Responses.ApiResponse<Responses.BillHead>> update(@PathVariable Long id, @Valid @RequestBody Requests.BillHeadUpdate r) { return ResponseHandler.response(HttpStatus.OK, "Bill head updated successfully", Responses.head(heads.update(id, r))); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { heads.delete(id); }
}
