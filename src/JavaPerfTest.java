import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

class JavaPerfTest
{
    public static void main(String[] args)
    {
        long numCycles = 50000L;
        Long startNano;
        Long endNano;
        Long sNano;
        Long eNano;
        List<Long> divMethodTimes = new ArrayList<Long>();
        List<Long> biMethodTimes = new ArrayList<Long>();
        List<Long> mathMethodTimes = new ArrayList<Long>();

        long longVal = 0L;
        long lowerBound = Long.MAX_VALUE / 2;
        long upperBound = Long.MAX_VALUE;
        long resultVal = 0L;

        sNano = System.nanoTime();
        for(long i=2; i<numCycles; i++)
        {
            longVal = lowerBound + (long)(Math.random() * (upperBound - lowerBound));
            startNano = System.nanoTime();
            resultVal = longVal * i;
            if(resultVal / i != longVal)
            {
                // overflow occurred
                endNano = System.nanoTime();
                Long duration = endNano - startNano;
                divMethodTimes.add(duration);
            }
        }
        eNano = System.nanoTime();
        System.out.println("div method raw avg = " + Long.toString((eNano - sNano) / (long)divMethodTimes.size()) + " ns");

        BigInteger resultValBI = BigInteger.valueOf(0);
        sNano = System.nanoTime();
        for(long i=2; i<numCycles; i++)
        {
            longVal = lowerBound + (long)(Math.random() * (upperBound - lowerBound));
            startNano = System.nanoTime();
            try {
                resultValBI = BigInteger.valueOf(longVal).multiply(BigInteger.valueOf(i));
                resultVal = resultValBI.longValueExact();
            }
            catch (ArithmeticException ae) {
                // overflow occurred
                endNano = System.nanoTime();
                Long duration = endNano - startNano;
                biMethodTimes.add(duration);
            }
        }
        eNano = System.nanoTime();
        System.out.println("bi method raw avg = " + Long.toString((eNano - sNano) / (long)biMethodTimes.size()) + " ns");

        sNano = System.nanoTime();
        for(long i=2; i<numCycles; i++)
        {
            longVal = lowerBound + (long)(Math.random() * (upperBound - lowerBound));
            startNano = System.nanoTime();
            try {
                resultVal = Math.multiplyExact(longVal, i);
            }
            catch (ArithmeticException ae) {
                // overflow occurred
                endNano = System.nanoTime();
                Long duration = endNano - startNano;
                mathMethodTimes.add(duration);
            }
        }
        eNano = System.nanoTime();
        System.out.println("math method raw avg = " + Long.toString((eNano - sNano) / (long)mathMethodTimes.size()) + " ns");

        divMethodTimes = removeOutliers(divMethodTimes);
        biMethodTimes = removeOutliers(biMethodTimes);
        mathMethodTimes = removeOutliers(mathMethodTimes);

        System.out.println("Divide method times -> " + calculateDurations(divMethodTimes));
        System.out.println("BigInteger method times -> " + calculateDurations(biMethodTimes));
        System.out.println("MultiplyExact method times -> " + calculateDurations(mathMethodTimes));

        try {
            outputToCSV("C:\\Users\\jerry\\OneDrive\\Documents\\javaTestOutputDivMethod.csv", divMethodTimes);
            outputToCSV("C:\\Users\\jerry\\OneDrive\\Documents\\javaTestOutputBiMethod.csv", biMethodTimes);
            outputToCSV("C:\\Users\\jerry\\OneDrive\\Documents\\javaTestOutputMathMethod.csv", mathMethodTimes);
        }
        catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    private static String calculateDurations(List<Long> list)
    {
        Long sum = 0L;
        Long min = Long.MAX_VALUE;
        Long max = Long.MIN_VALUE;
        Double stddev = 0.0;
        for (Long val : list)
        {
            sum += val;
            if (val < min){
                min = val;
            }
            else if (val > max){
                max = val;
            }
        }
        Double avg = (double)sum / (double)list.size();
        for(Long val : list) {
            stddev += Math.pow((double)val - avg, 2);
        }
        stddev = Math.sqrt(stddev / list.size());
        StringBuilder sb = new StringBuilder();
        sb.append("min = ");
        sb.append(Long.toString(min));
        sb.append(" ns | avg = ");
        sb.append(Double.toString(avg));
        sb.append(" ns | max = ");
        sb.append(Long.toString(max));
        sb.append(" ns | stddev = ");
        sb.append(Double.toString(stddev));
        sb.append(" ns");
        return sb.toString();
    }

    private static void outputToCSV(String filePath, List<Long> list) throws IOException
    {
        File csvFile = new File(filePath);
        FileWriter fw = null;
        Long cnt = 0L;
        try {
            fw = new FileWriter(csvFile);
            StringBuilder sb = new StringBuilder();
            for (Long val : list)
            {
                sb.append(Long.toString(cnt++));
                sb.append(", ");
                sb.append(Long.toString(val));
                sb.append("\n");
                
            }
            fw.write(sb.toString().trim());
        }
        finally {
            if (fw != null)
                fw.close();
        }
    }

    private static List<Long> removeOutliers(List<Long> list)
    {
        // remove anything outside 3 std deviations from mean
        // this preserves 99.7% data and should only remove a few large outliers
        double numStdDevs = 3.0;

        Long min = Long.MAX_VALUE;
        Long max = Long.MIN_VALUE;
        Long sum = 0L;
        Double stddev = 0.0;
        List<Long> retVal = new ArrayList<Long>();

        for (Long val : list)
        {
            sum += val;
            if (val < min){
                min = val;
            }
            else if (val > max){
                max = val;
            }
        }
        Double avg = (double)sum / (double)list.size();
        for(Long val : list) {
            stddev += Math.pow((double)val - avg, 2);
        }
        stddev = Math.sqrt(stddev / list.size());

        for (Long val : list)
        {
            if (Math.abs(val - avg) < stddev * numStdDevs)
            {
                retVal.add(val);
            }
        }

        int valsElim = list.size() - retVal.size();
        System.out.println("Eliminated " + valsElim + " values outside " + numStdDevs + " std deviations from mean.");

        return retVal;
    }
}