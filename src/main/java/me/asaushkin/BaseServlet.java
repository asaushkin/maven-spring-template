package me.asaushkin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Servlet implementation class BaseServlet
 */
public class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    
    private Integer lastInteger;
    private List<Integer> lastFactors;

    private long hits;
    private long cachehits;
    
    public synchronized long getHits() { return hits; }
    public synchronized double getCachHitRatio() {
        return ((double)cachehits) / hits;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long start = System.nanoTime();
        
        Integer i = extractFromRequest(request);
        List<Integer> factors = null;
        
        synchronized (this) {
            ++hits;
            
            if (i.equals(lastInteger)) {
                ++cachehits;
                factors = new ArrayList<>(lastFactors);
                
                logger.debug("The value recieved from the cache: Timing: {} {}", System.nanoTime() - start, this);
            }
        }
        
        if (factors == null) {
            factors = factor(i);
            
            synchronized (this) {
                lastInteger = i;
                lastFactors = new ArrayList<>(factors);
            }

            logger.debug("The value calculated without the cache: Timing: {} {}", System.nanoTime() - start, this);
        }
        
        encodeIntoResponse(response, factors, i);
    }
    
    private void encodeIntoResponse(HttpServletResponse response, List<Integer> factors, Integer number) throws IOException {
        response.getWriter().format("Factor %s is: %s", number, factors.size());
    }

    private Integer extractFromRequest(HttpServletRequest request) {
        return 100_000;
    }
    
    private boolean isPrime(int number) {
        for (int i = 2; i < number; ++i) {
            if (number % i == 0)
                return false;
        }
        return true;
    }
    
    private List<Integer> factor(Integer to) {
        return IntStream.rangeClosed(1, to).parallel().filter(i -> isPrime(i)).boxed()
                .collect(Collectors.toList());
    }
}
