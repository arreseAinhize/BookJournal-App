package eus.arreseainhize.bookjounal;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavView;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabMenu;
    private AppBarConfiguration appBarConfiguration;
    private TextView txtUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fabMenu = findViewById(R.id.fab_menu);
        bottomNavView = findViewById(R.id.bottom_nav_view);

        // Configurar header del drawer
        View headerView = navigationView.getHeaderView(0);
        txtUserEmail = headerView.findViewById(R.id.txt_user_email);

        // Configurar navegación
        setupNavigation();

        // Configurar FAB para abrir menú
        setupFloatingActionButton();

        // Verificar autenticación
        checkAuthentication();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Configurar AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.readingLogFragment,
                R.id.readingSystemFragment,
                R.id.wishListFragment,
                R.id.bookInfoFragment,
                R.id.favoriteBooksFragment
        )
                .setDrawerLayout(drawerLayout)
                .build();

        // Conectar navegación
        NavigationUI.setupWithNavController(bottomNavView, navController);
        NavigationUI.setupWithNavController(navigationView, navController);

        // ============================================================
        // OBSERVAR CAMBIOS DE DESTINO PARA ACTUALIZAR LA VISIBILIDAD
        // ============================================================
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            // Actualizar email en header si está autenticado
            if (mAuth.getCurrentUser() != null && txtUserEmail != null) {
                txtUserEmail.setText(mAuth.getCurrentUser().getEmail());
            }

            // ============================================================
            // FRAGMENTS PRINCIPALES
            // ============================================================
            if (destId == R.id.readingLogFragment ||
                    destId == R.id.readingSystemFragment ||
                    destId == R.id.wishListFragment ||
                    destId == R.id.bookInfoFragment ||
                    destId == R.id.favoriteBooksFragment) {

                // Mostrar bottom navigation y FAB según orientación
                updateMenuVisibilityByOrientation();

                // Desbloquear drawer
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                Log.d(TAG, "Fragment principal: mostrando menús");

                // ============================================================
                // FRAGMENTS DE AUTENTICACIÓN
                // ============================================================
            } else if (destId == R.id.fristFragment ||
                    destId == R.id.logInFragment ||
                    destId == R.id.singUpFragment) {

                // Ocultar todos los menús
                bottomNavView.setVisibility(View.GONE);
                fabMenu.setVisibility(View.GONE);

                // Bloquear drawer
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                Log.d(TAG, "Fragment de autenticación: menús ocultos");

                // ============================================================
                // OTROS FRAGMENTS
                // ============================================================
            } else {
                // Ocultar todos los menús
                bottomNavView.setVisibility(View.GONE);
                fabMenu.setVisibility(View.GONE);

                // Bloquear drawer
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                Log.d(TAG, "Otro fragment: menús ocultos");
            }
        });
    }

    // ============================================================
    // ACTUALIZAR VISIBILIDAD SEGÚN ORIENTACIÓN
    // ============================================================
    private void updateMenuVisibilityByOrientation() {
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Modo horizontal: ocultar bottom nav, mostrar FAB
            bottomNavView.setVisibility(View.GONE);
            fabMenu.setVisibility(View.VISIBLE);
            Log.d(TAG, "LANDSCAPE: BottomNav GONE, FAB VISIBLE");
        } else {
            // Modo vertical: mostrar bottom nav, ocultar FAB
            bottomNavView.setVisibility(View.VISIBLE);
            fabMenu.setVisibility(View.GONE);
            Log.d(TAG, "PORTRAIT: BottomNav VISIBLE, FAB GONE");
        }
    }

    private void setupFloatingActionButton() {
        fabMenu.setOnClickListener(v -> {
            toggleDrawer();
        });
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            drawerLayout.openDrawer(navigationView);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) ||
                super.onSupportNavigateUp();
    }

    // ============================================================
    // MANEJAR CAMBIOS DE ORIENTACIÓN
    // ============================================================
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Actualizar visibilidad según la nueva orientación
        if (navController != null && navController.getCurrentDestination() != null) {
            int currentDest = navController.getCurrentDestination().getId();

            // Solo actualizar si estamos en un fragment principal
            if (currentDest == R.id.readingLogFragment ||
                    currentDest == R.id.readingSystemFragment ||
                    currentDest == R.id.wishListFragment ||
                    currentDest == R.id.bookInfoFragment ||
                    currentDest == R.id.favoriteBooksFragment) {

                updateMenuVisibilityByOrientation();
            }
        }
    }

    // Métodos públicos para control desde fragments
    public void setBottomNavigationVisibility(boolean visible) {
        if (bottomNavView != null) {
            bottomNavView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setFabMenuVisibility(boolean visible) {
        if (fabMenu != null) {
            fabMenu.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(navigationView);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(navigationView);
        }
    }

    // Getters
    public BottomNavigationView getBottomNavView() {
        return bottomNavView;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public FloatingActionButton getFabMenu() {
        return fabMenu;
    }

    private void checkAuthentication() {
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "Usuario autenticado: " + mAuth.getCurrentUser().getEmail());
        } else {
            Log.d(TAG, "Usuario no autenticado");
        }
    }
}