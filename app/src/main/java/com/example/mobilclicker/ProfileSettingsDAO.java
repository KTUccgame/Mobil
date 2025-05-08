package com.example.mobilclicker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface ProfileSettingsDAO {
    @Insert
    void insert(ProfileSettings profile);
    @Update
    void updateProfile(ProfileSettings profile);
    @Query("SELECT * FROM ProfileSettings")
    List<ProfileSettings> getAll();
    @Query("SELECT * FROM ProfileSettings WHERE id IN (:userIds)")
    List<ProfileSettings> loadAllByIds(int[] userIds);
    @Query("SELECT * FROM ProfileSettings WHERE username LIKE :usrname LIMIT 1")
    ProfileSettings findByName(String usrname);
    @Insert
    void insertAll(ProfileSettings... users);
    @Delete
    void delete(ProfileSettings user);

    @Query("SELECT adminCheck FROM ProfileSettings WHERE id = :profileId LIMIT 1")
    boolean isUserAdmin(long profileId);

    @Update
    void updateAdminStatus(ProfileSettings profile);

    @Query("SELECT * FROM ProfileSettings WHERE id = :profileId LIMIT 1")
    ProfileSettings findById(long profileId);
}
