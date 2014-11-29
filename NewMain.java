/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pipinan.githubcrawler;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        try {
            GithubCrawler c = new GithubCrawler("pipinan@gmail.com", "******");        
            c.crawlRepoZip("douglascrockford", "JSON-java", "d:\\");
        } catch (IOException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }   
   
}
