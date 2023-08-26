package com.driver.model;

public enum CountryName {
    IND("001"),
    USA("002"),
    AUS("003"),
    CHI("004"),
    JPN("005");

    private final String code;

    private CountryName(String s) {
        code = s;
    }

    public String toCode() {
        return this.code;
    }
    public static CountryName fromCode(String countryName)
    {
        for(CountryName country :CountryName.values())
        {
            if(country.code.equals(countryName)){
                return country;
            }
        }
        return null;
    }

    public static CountryName fromStringToCountryName(String countryName)
    {
        for(CountryName country :CountryName.values())
        {
            if(countryName.equals(country.toString()))
            {
                return country;
            }
        }
        return null;
    }
}
