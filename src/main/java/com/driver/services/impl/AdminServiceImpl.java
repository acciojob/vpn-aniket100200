package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService
{
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password)
    {
        //create an admin and return
        Admin admin=new Admin();
        admin.setUserName(username);
        admin.setPassword(password);
       admin=adminRepository1.save(admin);

       return admin;

    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName)
    {
        Admin admin=adminRepository1.findById(adminId).get();
        if(admin==null)return null;

        ServiceProvider serviceProvider=serviceProviderRepository1.findServiceProviderByName(providerName);
        if(serviceProvider==null)
        {
            serviceProvider = new ServiceProvider();
            serviceProvider.setName(providerName);
        }


        admin.getServiceProviders().add(serviceProvider);
        serviceProvider.setAdmin(admin);
        admin=adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception
    {
        //add a country under the serviceProvider and return respective service provider
        //country name would be a 3-character string out of ind, aus, usa, chi, jpn. Each character can be in uppercase or lowercase. You should create a new Country object based on the given country name and add it to the country list of the service provider. Note that the user attribute of the country in this case would be null.
        //In case country name is not amongst the above mentioned strings, throw "Country not found" exception

        Optional<ServiceProvider>serviceProviderOptional=serviceProviderRepository1.findById(serviceProviderId);
        if(serviceProviderOptional.isPresent()==false)throw new Exception("Service Provider Not Found");

        ServiceProvider serviceProvider=serviceProviderOptional.get();
        Country country=new Country();

        countryName=countryName.toUpperCase();

        CountryName countryName1=CountryName.fromStringToCountryName(countryName);

            if(countryName1==null)throw  new Exception("Country not found");

            country.setCountryName(countryName1);
            country.setServiceProvider(serviceProvider);
            country.setCode(countryName1.toCode());
            country =countryRepository1.save(country);
            serviceProvider.getCountryList().add(country);

           serviceProvider= serviceProviderRepository1.save(serviceProvider);
           return serviceProvider;


    }
}
