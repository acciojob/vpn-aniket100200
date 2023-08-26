package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception
    {

        //create a user of given country. The originalIp of the user should be "countryCode.userId" and return the user.
        // Note that right now user is not connected and thus connected would be false and maskedIp would be null
            User user=new User();
            user.setConnected(false);

           CountryName countryName1= CountryName.fromStringToCountryName(countryName);
           String countryCode=countryName1.toCode();

           //set the country..
            user.setMaskedIp(null);

            Country country=new Country();
            country.setCode(countryCode);
            country.setCountryName(countryName1);
            countryRepository3.save(country);

            user=userRepository3.save(user);

            user.setOriginalIp(countryCode+"."+user.getId());

            user.setUserName(username);
            user.setPassword(password);

            user= userRepository3.save(user);

            return user;

    }



    @Override
    public User subscribe(Integer userId, Integer serviceProviderId)
    {
        Optional<User>optionalUser=userRepository3.findById(userId);
        if(!optionalUser.isPresent())return null;

        User user=optionalUser.get();

        Optional<ServiceProvider>serviceProviderOptional=serviceProviderRepository3.findById(serviceProviderId);
        if(!serviceProviderOptional.isPresent())return user;

        ServiceProvider serviceProvider=serviceProviderOptional.get();

        //subscribe to the serviceProvider by adding it to the list of providers and return updated User

        user.getServiceProviderList().add(serviceProvider);

        //bidirectional..
        serviceProvider.getUsers().add(user);

     user= userRepository3.save(user);
        return user;

    }
}
