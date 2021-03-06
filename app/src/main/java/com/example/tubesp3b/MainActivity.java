package com.example.tubesp3b;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentResultListener;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.example.tubesp3b.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity{
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(this.getLayoutInflater());
        setContentView(binding.getRoot());

        this.toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this, drawer, this.toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawer.addDrawerListener(abdt);
        abdt.syncState();

        this.presenter = new MainPresenter(this);
        presenter.getMainPage();
        this.getSupportFragmentManager().setFragmentResultListener("addMovie", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey , Bundle result){
                String judul = result.getString("judul");
                String sinopsis = result.getString("sinopsis");
                String poster = result.getString("poster");
                Movie newMovie = new Movie(judul,sinopsis, poster, "", 0,"");
                presenter.addList(newMovie);
                presenter.changePage(2);
            }
        });
        this.getSupportFragmentManager().setFragmentResultListener("deleteMovie", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey , Bundle result){
                Movie movie = result.getParcelable("deleteMovie");
                presenter.deleteList(movie);
            }
        });
        this.getSupportFragmentManager().setFragmentResultListener("changeList", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(String requestKey , Bundle result){
                Movie movie = result.getParcelable("editedMovie");
                int moviePosition = result.getInt("position");
                presenter.changeList(movie, moviePosition);
            }
        });
        this.getSupportFragmentManager().setFragmentResultListener("changePage", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int page = result.getInt("page");
                presenter.changePage(page);
                drawer.closeDrawers();
            }
        });
    }
    private void storeInternal(String param, String filename, int opt) {
        File file = null;
        if(opt == 1) {
            file = new File(this.getFilesDir(), filename);
        }
        else {
            file = new File(this.getCacheDir(), filename);
        }
        this.writeFile(file, param);
    }

    private String loadInternal(String filename, int opt) {
        File file = null;
        if(opt == 1) {
            file = new File(this.getFilesDir(), filename);
        }
        else {
            file = new File(this.getCacheDir(), filename);
        }
        return this.readFile(file);
    }

    private void writeFile(File file, String param) {
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.append(param);
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            Log.d("io_error", e.getMessage());
        }
        Log.d("storage_path", file.getAbsolutePath());
    }
    private String readFile(File file) {
        String data = "";
        try {
            Scanner scanner = new Scanner(file);
            data = scanner.next();
            scanner.close();
        }
        catch (FileNotFoundException e) {
            Log.d("io_error", e.getMessage());
        }
        return data;
    }



    public int getFrameLayoutId() {
        return binding.fragmentContainer.getId();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.presenter.saveList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.getFilmListAdapter().getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", true);
        editor.apply();
    }
}