package com.avaya.oceanareferenceclient.settings.pojos;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EncryptionPreferences {


    private Set<String> encryptionTypes;
    private boolean isEncryptionEnabled;


    private boolean available;

    public boolean isEncryptionEnabled() {
        return isEncryptionEnabled;
    }

    public boolean isAvailable() {
        return available;
    }


    public Set<String> getEncryptionTypes() {
        return encryptionTypes;
    }

    public void setEncryptionTypes(Set<String> encryptionTypes){
        this.encryptionTypes = encryptionTypes;
    }

    public void setEncryptionEnabled(boolean isEncryptionEnabled){
        this.isEncryptionEnabled = isEncryptionEnabled;
    }

    public void setAvailable(boolean bAvailable) {
        this.available = bAvailable;
    }


}
