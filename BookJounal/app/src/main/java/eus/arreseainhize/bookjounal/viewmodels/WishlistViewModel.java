package eus.arreseainhize.bookjounal.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import eus.arreseainhize.bookjounal.models.WishlistItem;

public class WishlistViewModel extends ViewModel {

    private final MutableLiveData<List<WishlistItem>> wishlistItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Getter para lista
    public LiveData<List<WishlistItem>> getWishlistItems() {
        return wishlistItems;
    }

    // Setter para lista completa
    public void setWishlistItems(List<WishlistItem> items) {
        wishlistItems.setValue(items);
    }

    // AÑADIR UN ITEM (el que falta)
    public void addWishlistItem(WishlistItem item) {
        List<WishlistItem> currentList = wishlistItems.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(item);
        wishlistItems.setValue(currentList);
    }

    // eliminar un item por posición
    public void removeItem(int position) {
        List<WishlistItem> currentList = wishlistItems.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            wishlistItems.setValue(currentList);
        }
    }

    // eliminar un item por ID
    public void removeItemById(String itemId) {
        List<WishlistItem> currentList = wishlistItems.getValue();
        if (currentList != null && itemId != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (itemId.equals(currentList.get(i).getId())) {
                    currentList.remove(i);
                    wishlistItems.setValue(currentList);
                    break;
                }
            }
        }
    }

    // Getters y setters
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setError(String error) {
        errorMessage.setValue(error);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    // Metodo para limpiar lista
    public void clearWishlist() {
        wishlistItems.setValue(new ArrayList<>());
    }
}