package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Utils {
	static public ArrayList<Integer> getListRandomNumbers(int size, int max_number)
	{
		ArrayList<Integer> listRandomNumbers = new ArrayList<Integer>();
		Random random = new Random();
		HashSet<Integer> setCheckDuplicate = new HashSet<Integer>();
		while(listRandomNumbers.size()<size)
		{
			int number = random.nextInt(max_number);
			if(!setCheckDuplicate.contains(number))
			{
				setCheckDuplicate.add(number);
				listRandomNumbers.add(number);
			}
		}
		
		return listRandomNumbers;
	}
	
	public static void main(String [] args)
	{
		ArrayList<Integer> listRandomNumber = getListRandomNumbers(5, 10);
		System.out.println(listRandomNumber);
	}
}
