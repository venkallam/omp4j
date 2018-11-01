
import org.omp4j.*;
import org.abego.*;
import scala.*;
import org.antlr.*;

public class StartingPoint {

	public static void main(String[] args) {
		
		// omp parallel
		{
			//long currThread = Thread.currentThread().getId();
			//long numThreads = Thread.activeCount();
				
			System.out.println( "Hello From Thread "+ OMP4J_THREAD_NUM +" of " + OMP4J_NUM_THREADS);
					
		}
		System.out.println("") ;
		
		

	}

}


//https://github.com/venkallam/assignment.git