// viewmodels/UserViewModel.java (Completo)
package eus.arreseainhize.bookjounal.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import eus.arreseainhize.bookjounal.models.User;

public class UserViewModel extends ViewModel {

    private final MutableLiveData<FirebaseUser> firebaseUser = new MutableLiveData<>();
    private final MutableLiveData<User> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
    private final MutableLiveData<String> userEmail = new MutableLiveData<>("");

    public LiveData<FirebaseUser> getFirebaseUser() {
        return firebaseUser;
    }

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public void setFirebaseUser(FirebaseUser user) {
        firebaseUser.setValue(user);
        isAuthenticated.setValue(user != null);
        if (user != null) {
            userEmail.setValue(user.getEmail());
        } else {
            userEmail.setValue("");
        }
    }

    public void setUserProfile(User profile) {
        userProfile.setValue(profile);
    }

    public void logout() {
        firebaseUser.setValue(null);
        userProfile.setValue(null);
        isAuthenticated.setValue(false);
        userEmail.setValue("");
    }
}