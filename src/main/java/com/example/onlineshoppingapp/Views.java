package com.example.onlineshoppingapp;

public class Views {
    // Basic fields visible to everyone (User)
    public interface PublicView {}

    // Detailed fields visible only to the Admin, extending PublicView
    public interface AdminView extends PublicView {}
}