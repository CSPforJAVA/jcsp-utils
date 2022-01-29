/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jcsp.helpers;

import jcsp.lang.CSProcess;
import jcsp.lang.Parallel;

/**
 * Same as Parallel, but stores the thread name on run, and sets it on exit.
 * //TODO Should probably also do so for the threads it creates
 * 
 * @author erhannis
 */
public class NameParallel extends Parallel {
    public NameParallel() {
        super();
    }
    
    public NameParallel(CSProcess[] processes) {
        super(processes);
    }

    public NameParallel(CSProcess[][] processes) {
        super(processes);
    }
    
    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        try {
            super.run();
        } finally {
            try {
                Thread.currentThread().setName(name);
            } catch (Throwable t) {
                // Nothing
            }
        }
    }
}
