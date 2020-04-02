package com.sbs.sbsgroup7.service;

//import com.sbs.sbsgroup7.DataSource.RequestRepository;
import com.sbs.sbsgroup7.DataSource.RequestRepository;
import com.sbs.sbsgroup7.dao.RequestDaoInterface;
import com.sbs.sbsgroup7.dao.UserDaoInterface;
import com.sbs.sbsgroup7.model.Account;
import com.sbs.sbsgroup7.model.Request;
import com.sbs.sbsgroup7.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Repository
public class RequestService {

    //private final UserDaoInterface userDao;

    @Autowired
    private RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository=requestRepository;

    }

    public Request createRequest(User requestedUser, Request request){
        Request r = new Request();
        r.setRequestedUser(requestedUser);
        r.setApprovedUser(null);
        r.setRequestType(request.getRequestType());
        r.setRequestedTime(Instant.now());
        r.setRequestStatus("pending");
        requestRepository.save(r);
        return r;
    }

    public Request createChequeRequest(User requestedUser, Account account, Request request){
        Request r = new Request();
        r.setRequestedUser(requestedUser);
        r.setApprovedUser(null);
        r.setRequestType("cheque");
        r.setRequestedTime(Instant.now());
        r.setRequestStatus("pending");
        r.setAccount(account);
        requestRepository.save(r);
        return r;
    }

    public Request findRequestById(long requestId)
    {
        return requestRepository.findByRequestId(requestId);
    }

    public void deleteByRequestId(long requestId)
    {
        requestRepository.deleteByRequestId(requestId);
    }

    public List<Request> findAll()
    {
        return requestRepository.findAll();
    }
    public List<Request> findPendingRequests()
    {
        List<Request> repos=requestRepository.findByRequestStatus("pending");
        List<Request>  requests= new ArrayList<Request>();
        for(Request request : repos)
        {
            if(!(request.getRequestType().equals("cheque"))){
                requests.add(request);
        }
        }
        return requests;
    }

    public List<Request> findPendingChequeRequests()
    {
        return requestRepository.findByRequestStatusAndRequestType("pending", "cheque");
    }


}