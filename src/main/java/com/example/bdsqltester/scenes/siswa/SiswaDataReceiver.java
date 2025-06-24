package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.dtos.User;

// Interface ini memastikan setiap controller siswa punya metode setUser
public interface SiswaDataReceiver {
    void setUser(User user);
}