package com.sbs.sbsgroup7.api;


import com.sbs.sbsgroup7.DataSource.SessionLogRepository;
import com.sbs.sbsgroup7.DataSource.AppointmentRepository;
import com.sbs.sbsgroup7.model.*;

import com.sbs.sbsgroup7.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/user")
@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SessionLogRepository sessionLogRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    public UserController(UserService userService)
    {
        this.userService=userService;
    }

    @RequestMapping("/home")
    public String userHome(Model model){
        User user = userService.getLoggedUser();
        List<SessionLog> sessionLogs = sessionLogRepository.findAll();
        if (sessionLogs != null) {
            sessionLogs = sessionLogs
                    .stream()
                    .filter(e -> e.getUserId() != null)
                    .filter(e -> e.getUserId().equals(user.getUserId()))
                    .sorted((s1, s2) -> s1.getTimestamp().compareTo(s2.getTimestamp()))
                    .collect(Collectors.toList());

            model.addAttribute("lastAccess", sessionLogs.get(0).getTimestamp());

        } else {
            model.addAttribute("lastAccess", "Never");
        }

        return "user/home" ;
    }

    @RequestMapping("/profile")
    public String userProfile(Model model){
        User user = userService.getLoggedUser();
        model.addAttribute("profile", user);
        return "user/profile";
    }

    @RequestMapping("/accounts")
//    public String approveRequests(Model model) {
//        User user = userService.getLoggedUser();
//        model.addAttribute("accounts", accountService.findByUser(user));
    public String getAccounts(Model model) {
        User user=userService.getLoggedUser();
        model.addAttribute("accounts", accountService.getAccountsByUser(user));

        return "user/accounts";
    }

    //requesting new bank account creations
    @GetMapping("/createAccount")
    public String createAccount(Model model){
        model.addAttribute("request", new Request());
        return "user/createAccount";
    }
    @PostMapping("/createAccount")
    public String createAccount(@ModelAttribute("request") Request request){
        try {
            User user = userService.getLoggedUser();
            requestService.createRequest(user, request);
            return "user/accountRequestSent";
        } catch(Exception e) {
            return e.getMessage();
        }
    }
    @GetMapping("/viewCheque/{id}")
    public String viewCheques(@PathVariable("id") Long id, Model model){
        User user=userService.getLoggedUser();
        List<Account> accts=accountService.getAccountsByUser(user);
        Account account=accountService.getAccountByAccountNumber(id);
        for (Account acct: accts){
            if(acct==account){
                List<Cheque> cheques=accountService.findChequeByAccount(account);
                model.addAttribute("cheques", cheques);
                return "user/viewCheque";
            }
        }
        return "redirect:/user/error";
    }

    @RequestMapping("/requestCheque/{id}")
    public String requestCheques(@PathVariable("id") Long id){
            User user = userService.getLoggedUser();
            Account account=accountService.getAccountByAccountNumber(id);
            List<Account> accts=accountService.getAccountsByUser(user);
            for (Account acct: accts){
                if(acct==account){
                   Request request=new Request();
                    requestService.createChequeRequest(user, acct, request);
                    return "user/chequeRequestSent";
                    }
                }
            return "redirect:/user/error";
    }

    @GetMapping("/creditdebit")
    public String debit(Model model){
        model.addAttribute("creditdebit", new CreditDebit());
        return "user/creditdebit";
    }

    @PostMapping("/creditdebit")
    public String debit(@Valid @ModelAttribute("creditdebit") CreditDebit creditDebit, BindingResult result){
        User user = userService.getLoggedUser();
        if (result.hasErrors()) {
            result.getAllErrors().stream().forEach(System.out::println);
            return "user/creditDebit";
        }
        try {
            accountService.creditDebitTransaction(user,creditDebit);
            if(creditDebit.getAmount() >= 1000) {
                return "user/accountRequestSent";
            }
            else {
                return "redirect:/user/accounts";
            }
        } catch(Exception e) {
            return "redirect:/user/error";
        }

    }
    @GetMapping("/transferFunds")
    public String transferFunds(Model model){
        model.addAttribute("transfer", new TransactionPage());
        return "user/transferFunds";
    }

    @PostMapping("/transferFunds")
    public String transferFunds(@Valid @ModelAttribute("transfer") TransactionPage transactionPage, BindingResult result) {
        User user = userService.getLoggedUser();
        if (result.hasErrors()) {
            result.getAllErrors().stream().forEach(System.out::println);
            return "user/transferFunds";
        }
        try {
            accountService.transferFunds(user, transactionPage);
            if (transactionPage.getAmount() >= 1000) {
                return "user/accountRequestSent";
            }
            else {
                return "redirect:/user/accounts";
            }
        } catch (Exception e) {
            return "redirect:/user/error";
        }
    }

    @GetMapping("/emailTransfer")
    public String emailTransfer(Model model){
        model.addAttribute("email", new EmailPage());
        return "user/emailTransfer";
    }

    @PostMapping("/emailTransfer")
    public String emailTransfer(@Valid @ModelAttribute("email") EmailPage emailPage, BindingResult result) {
        User user = userService.getLoggedUser();
        if (result.hasErrors()) {
            result.getAllErrors().stream().forEach(System.out::println);
            return "user/emailTransfer";
        }
        try {
            accountService.emailTransfer(user, emailPage);
            if (emailPage.getAmount() >= 1000) {
                return "user/accountRequestSent";
            }
            else {
                return "redirect:/user/accounts";
            }
        } catch (Exception e) {
            return "redirect:/user/error";
        }
    }


    @PostMapping("/add")
    public void addUser(@NotNull @Validated @RequestBody User user){
        userService.add(user);
    }

    @PutMapping ("/update")
    public void update(@NotNull @Validated @RequestBody User user){
        userService.update(user);
    }

    @DeleteMapping(path="/remove/{id}")
    public void deleteUserById(@PathVariable("id") String id){
        userService.delete(id);
    }

    @GetMapping(path = "/")
    public List<User> getAllUsers(){
        return userService.findAll();
    }

    @DeleteMapping(path="/removeAll")
    public void deleteAll(){
        userService.deleteAll();
    }

    @RequestMapping("/error")
    public String error(){
        return "user/error";
    }


    @GetMapping("/createAppointment")
    public String createAppointment(Model model){
        User user =userService.getLoggedUser();
        Appointment appointment=appointmentRepository.findByUser(user).orElse(null);
        if(appointment==null){
            model.addAttribute("scheduleApp", new Appointment());
            return "user/appointment";}
        else{
            return "user/appointmentExists";
        }
    }

    @PostMapping("/createAppointment")
    public String createAppointment(@ModelAttribute("scheduleApp") Appointment appointment){
        User user = userService.getLoggedUser();
        System.out.println(user.getUserId());
        appointmentService.createAppointment(user, appointment);
        return "redirect:/user/viewAppointment";
    }

    @GetMapping("/updateProfile")
    public String updateProfile(Model model){
        User currentUser = userService.getLoggedUser();
        model.addAttribute("updateProf", currentUser);
        return "user/updateProfile";
    }

    @PostMapping("/updateProfile")
    public String createAppointment(@ModelAttribute("updateProf") User user){
        userService.updateInformation(user);

        return "user/profileUpdated";
    }

    @RequestMapping("/support")
    public String userSupport(){
        return "user/support" ;
    }

    @RequestMapping("/viewAppointment")
    public String getAppointment(Model model) {
        User user=userService.getLoggedUser();
        Appointment appointment=appointmentRepository.findByUser(user).orElse(null);
        if(appointment==null){
            return "user/noAppointment";
        }
        else {
            model.addAttribute("app", appointment);
            return "user/viewAppointment";
        }
    }
}
