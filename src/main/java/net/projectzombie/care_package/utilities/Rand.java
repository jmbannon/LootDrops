/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.utilities;

import java.util.Random;

/**
 *
 * @author com.gmail.jbann1994
 */
public class Rand
{
    static private final Random RAND = new Random();
    
    static public int nextInt(final int max)
    {
        return RAND.nextInt(max);
    }
}
