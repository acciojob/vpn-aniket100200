package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.channels.AlreadyConnectedException;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception
    {
        //Connect the user to a vpn by considering the following priority order.
        //1. If the user is already connected to any service provider, throw "Already connected" exception.
        //2. Else if the countryName corresponds to the original country of the user, do nothing. This means that the user wants to connect to its original country, for which we do not require a connection. Thus, return the user as it is.
        //3. Else, the user should be subscribed under a serviceProvider having option to connect to the given country.
        //If the connection can not be made (As user does not have a serviceProvider or serviceProvider does not have given country, throw "Unable to connect" exception.
        //Else, establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and return the updated user. If multiple service providers allow you to connect to the country, use the service provider having smallest id.

        User user=userRepository2.findById(userId).get();
        if(user.getConnected())throw new Exception("Already connected");

        String country=user.getCountry().getCountryName().toCode();
        if(country.equals(CountryName.fromStringToCountryName(countryName).toCode()))return user;

        List<ServiceProvider>serviceProviders=user.getServiceProviderList();


        ServiceProvider provider=null;

        String countryCode=null;


        for(ServiceProvider serviceProvider:serviceProviders)
        {
            List<Country>countryList=serviceProvider.getCountryList();

            for(Country country1:countryList)
            {
                if(country1.getCountryName().toCode().equals(CountryName.fromStringToCountryName(countryName).toCode()))
                {
                    if(provider==null || provider.getId()>serviceProvider.getId())
                    {
                        provider=serviceProvider;
                        countryCode=country1.getCountryName().toCode();
                    }
                }
            }
        }
        if(provider!=null)
        {
           user.setMaskedIp(countryCode+"."+provider.getId()+"."+userId);
           Connection connection=new Connection();
           connection.setUser(user);
           connection.setServiceProvider(provider);
           user.setConnected(true);

          connection= connectionRepository2.save(connection);
            user.getConnectionList().add(connection);
           user =userRepository2.save(user);

        }
        else throw new Exception("Unable to connect");

        return user;

    }
    @Override
    public User disconnect(int userId) throws Exception
    {
        //If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null, update relevant attributes and return updated user.
        Optional<User>optionalUser=userRepository2.findById(userId);
        if(optionalUser.isPresent()==false)throw new Exception();

        User user=optionalUser.get();
        if(!user.getConnected())throw new Exception("Already disconnected");

        //disconnect it..
        user.setConnected(false);
        user.setMaskedIp(null);
        List<Connection> connectionList=user.getConnectionList();
        Connection connection=connectionList.get(connectionList.size()-1);
        connectionList.remove(connection);

        connectionRepository2.delete(connection);
        user=userRepository2.save(user);

        return  user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception
    {
        //Establish a connection between sender and receiver users
        //To communicate to the receiver, sender should be in the current country of the receiver.
        //If the receiver is connected to a vpn, his current country is the one he is connected to.
        //If the receiver is not connected to vpn, his current country is his original country.
        //The sender is initially not connected to any vpn. If the sender's original country does not match receiver's current country, we need to connect the sender to a suitable vpn. If there are multiple options, connect using the service provider having smallest id
        //If the sender's original country matches receiver's current country, we do not need to do anything as they can communicate. Return the sender as it is.
        //If communication can not be established due to any reason, throw "Cannot establish communication" exception

        Optional<User>optionalUser=userRepository2.findById(senderId);
        if(!optionalUser.isPresent())throw new Exception("Cannot establish communication");

        User sender=optionalUser.get();

        optionalUser=userRepository2.findById(receiverId);

        if(!optionalUser.isPresent())throw new Exception("Cannot establish communication");

        User receiver=optionalUser.get();

        String senderCountry=sender.getCountry().getCountryName().toCode();

        String receiverCountry=receiver.getCountry().getCountryName().toCode();
       if(receiver.getConnected())
       {
         String maskedIp=receiver.getMaskedIp();

          //arr[0] is Country Code..
           receiverCountry=maskedIp.substring(0,3);
       }
       if(senderCountry.equals(receiverCountry))
       {
           //we don't have to do nothing they can simply talk..
           return sender;
       }


       //else I'll have to check who'm to connect.. where to connect...
        CountryName countryName1=CountryName.fromCode(receiverCountry);
       try
       {
            sender = connect(senderId, "" + countryName1);
        }
       catch (Exception e)
       {
          //this means error Found
           //means connection can't be done..
           throw new Exception("Cannot establish communication");
       }
       return sender;


    }
}
