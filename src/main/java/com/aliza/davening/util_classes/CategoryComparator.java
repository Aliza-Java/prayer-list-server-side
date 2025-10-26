spackage com.aliza.davening.util_classes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Davenfor;

public class CategoryComparator implements Comparator<Davenfor> {

	private static final List<String> CUSTOM_ORDER = Arrays.asList("refua", "shidduchim", "banim", "soldiers");

	@Override
	public int compare(Davenfor d1, Davenfor d2) {

		int index1 = CUSTOM_ORDER.indexOf(d1.getCategory().toLowerCase());
		int index2 = CUSTOM_ORDER.indexOf(d2.getCategory().toLowerCase());

		// If a category is not in the custom list, place it after all known categories
		if (index1 == -1)
			index1 = Integer.MAX_VALUE;
		if (index2 == -1)
			index2 = Integer.MAX_VALUE;

		int result = Integer.compare(index1, index2);

        // If same category and category is "shidduchim" → apply Bat/Bas sorting 
		//todo: for some reason searching for 'בת' doesn't work.  not so crucial if there is always english name.
        if (result == 0 && "shidduchim".equalsIgnoreCase(d1.getCategory())) {
            return Utilities.batFirstComparator().compare(d1, d2);
        }

        return result;
	}
}
