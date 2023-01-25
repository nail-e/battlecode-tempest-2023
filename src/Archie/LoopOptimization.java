package Archie;

public class LoopOptimization{
    int[] values = new int[] { 1, 2 };


    public void loop(){
        for(int value : values){
            value = 0;
        }
    }

    public void loop2(){
        int len = values.length;
        for(int i=0; i<len; i++){
            values[i] = 0;
        }
    }

    public void loop3(){
        int[] local_values = values;
        int len = local_values.length;
        for (int i= 0 ; i<len; i++){
            local_values[i] = 0;
        }
    }

    public void loop4() {
        int[] local_values = values;
        for(int i = local_values.length; --i >= 0;){
            local_values[i] = 0;
        }
    }
}