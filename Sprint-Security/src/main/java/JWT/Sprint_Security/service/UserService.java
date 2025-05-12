package JWT.Sprint_Security.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import JWT.Sprint_Security.repository.UserRepository;
import JWT.Sprint_Security.model.User;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;

    }

    public List<User> getAllUsers(){

        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;

    }



}
