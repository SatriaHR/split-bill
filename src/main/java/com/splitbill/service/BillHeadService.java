package com.splitbill.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitbill.domain.BillHead;
import com.splitbill.repository.BillHeadRepository;
import com.splitbill.web.ApiException;
import com.splitbill.web.dto.Requests;

@Service
public class BillHeadService {
    private final BillHeadRepository heads;
    private final UserService users;
    public BillHeadService(BillHeadRepository heads, UserService users) { this.heads = heads; this.users = users; }
    
    @Transactional
    public BillHead create(Requests.BillHeadCreate request, Long creatorId) {
        var creator = users.locked(creatorId);
        var billHead = new BillHead(request.description(), creator);
        return heads.save(billHead);
    }

    @Transactional(readOnly = true)
    public BillHead get(Long id) {
        return heads.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill head not found"));
    }

    @Transactional(readOnly = true)
    public Iterable<BillHead> all() {
        return heads.findAll();
    }

    @Transactional
    public BillHead update(Long id, Requests.BillHeadUpdate request) {
        BillHead billHead = get(id);
        billHead.setDescription(request.description());
        return billHead;
    }

    public com.splitbill.domain.AppUser user(Long id) { return users.get(id); }

    @Transactional
    public void delete(Long id) {
        BillHead billHead = get(id);
        if (billHead.getDetails().stream().anyMatch(detail -> detail.getAmountPaid().signum() > 0)) {
            throw new ApiException(HttpStatus.CONFLICT, "Bill heads with paid details cannot be deleted");
        }
        heads.delete(billHead);
    }
}
