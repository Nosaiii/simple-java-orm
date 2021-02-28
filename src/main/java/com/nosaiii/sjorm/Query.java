package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.NoParameterlessConstructorException;
import main.java.com.nosaiii.sjorm.metadata.PivotModelMetadata;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

public class Query<T extends Model> {
    private List<T> collection;

    /**
     * The base constructor used upon instantiating a new instance of a {@link Query} by using the given {@link ResultSet} object and associated class of the model
     * @param resultSet The {@link ResultSet} object containing data of model instances
     * @param modelClass The class type of the model associated with this instance of the {@link Query} object
     * @throws NoParameterlessConstructorException Thrown when the given class type of the model has no parameterless constructors present
     */
    public Query(ResultSet resultSet, Class<T> modelClass) throws NoParameterlessConstructorException {
        collection = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Model model = null;

                if(modelClass.equals(PivotModel.class)) {
                    Constructor<T> pivotModelConstructor = modelClass.getConstructor(ResultSet.class, Class.class, Class.class);
                    PivotModelMetadata pivotModelMetadata = (PivotModelMetadata) SJORM.getInstance().getMetadata(modelClass);

                    model = pivotModelConstructor.newInstance(resultSet, pivotModelMetadata.getTypeLeft(), pivotModelMetadata.getTypeRight());
                } else {
                    Constructor<T> modelConstructor = modelClass.getConstructor(ResultSet.class);
                    model = modelConstructor.newInstance(resultSet);
                }

                //noinspection unchecked
                collection.add((T) model);
            }
        } catch(NoSuchMethodException e) {
            throw new NoParameterlessConstructorException(modelClass);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor used when converting an existing {@link List} to a new instance of a {@link Query}
     * @param fromList The list to convert
     */
    public Query(List<T> fromList) {
        collection = fromList;
    }

    /**
     * Constructor used to clone an existing {@link Query} to a new instance of a {@link Query}
     * @param base The base {@link Query} object to clone
     */
    private Query(Query<T> base) {
        collection = new ArrayList<>();
        collection.addAll(base.collection);
    }

    /**
     * Queries predicates over the collection to test if all of them successfully match
     * @param predicate The predicate to test on for every entry of the collection
     * @return True, if all entries match the given predicate. False, if atleast 1 fails the predicate.
     */
    public boolean all(Predicate<T> predicate) {
        for (T m : collection) {
            if (!predicate.test(m)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Queries predicates over the collection to test if any of them successfully match
     * @param predicate The predicate to test on for every entry of the collection
     * @return True, if atleast 1 entry matches the given predicate. False, if all entries fail the predicate.
     */
    public boolean any(Predicate<T> predicate) {
        for (T m : collection) {
            if (predicate.test(m)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the average value of all numeric entries in the collection
     * @param propertyName The name of the property to get the average from
     * @return A {@code double} of the average value of all numeric entries in the collection
     */
    public double average(String propertyName) {
        return sum(propertyName) / collection.size();
    }

    public boolean contains(T model) {
        for (T m : collection) {
            if (m == model) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sums up the amount of entries present in the collection
     * @return The total sum of the amount of entries present in the collection
     */
    public int count() {
        return collection.size();
    }

    /**
     * Removes duplicate entries from the collection
     * @return A cloned {@link Query} object without duplicate entries
     */
    public Query<T> distinct() {
        Query<T> cloned = clone();

        for (T m : collection) {
            if (!cloned.contains(m)) {
                cloned.collection.add(m);
            }
        }

        return cloned;
    }

    /**
     * Gets the first entry from the collection
     * @return The first entry from the collection
     */
    public T first() {
        if(collection.isEmpty()) {
            return null;
        }

        return collection.get(0);
    }

    /**
     * Gets the first entry from the collection after filtering using a predicate
     * @param predicate The predicate to filter out entries from the collection
     * @return The first entry from the collection after filtering using the predicate
     */
    public T firstOrDefault(Predicate<T> predicate) {
        return where(predicate).first();
    }

    /**
     * Gets the last entry from the collection
     * @return The last entry from the collection
     */
    public T last() {
        if(collection.isEmpty()) {
            return null;
        }

        return collection.get(collection.size() - 1);
    }

    /**
     * Groups the collection into a map by the given property as key
     * @param propertyName The name of the property to group on
     * @param <V> The type used as key for the grouped map
     * @return A {@link Map} object representing groups by the given property as key
     */
    public <V> Map<V, Query<T>> groupBy(String propertyName) {
        Map<V, Query<T>> groupedMap = new HashMap<>();

        for (T m : collection) {
            //noinspection unchecked
            V propertyValue = (V) m.getProperty(propertyName);

            Query<T> query = new Query<>(this);
            if (groupedMap.containsKey(propertyValue)) {
                query = groupedMap.get(propertyValue);
            }

            query.collection.add(m);
            groupedMap.put(propertyValue, query);
        }

        return groupedMap;
    }

    /**
     * Orders the collection by comparing the values of the given property
     * @param propertyName The property name to compare its values with
     * @param <V> The type of the property, used to compare with
     * @return A cloned {@link Query} object with ordered entries by the given property
     */
    public <V> Query<T> orderBy(String propertyName) {
        Query<T> cloned = clone();

        cloned.collection.sort((o1, o2) -> {
            Object property1 = o1.getProperty(propertyName), property2 = o2.getProperty(propertyName);

            if (!(property1 instanceof Comparable) || !(property2 instanceof Comparable)) {
                throw new IllegalArgumentException("Given properties can not be compared");
            }

            @SuppressWarnings("unchecked")
            Comparable<V> propertyComparable = (Comparable<V>) property1;
            //noinspection unchecked
            return propertyComparable.compareTo((V) property2);
        });

        return cloned;
    }

    /**
     * Orders the collection by comparing the values of the given property in a reversed order
     * @param propertyName The property name to compare its values with
     * @return A cloned {@link Query} object with ordered entries by the given property in a reversed order
     */
    public Query<T> orderByDescending(String propertyName) {
        return orderBy(propertyName).reverse();
    }

    /**
     * Reverses the order of the entries in the collection
     * @return A cloned {@link Query} object with the reversed order of the entries of the given collection
     */
    public Query<T> reverse() {
        Query<T> cloned = clone();
        cloned.collection = new ArrayList<>();

        for (int i = collection.size() - 1; i >= 0; i--) {
            cloned.collection.add(collection.get(i));
        }

        return cloned;
    }

    /**
     * Gets the maximum numeric value from a collection by the given property
     * @param propertyName The name of the property to get the maximum value from
     * @return A {@code double} of the maximum value present in the collection from the given property
     */
    public double max(String propertyName) {
        if(collection.isEmpty()) {
            return 0;
        }

        double highest = Double.MIN_VALUE;

        for(T m : collection) {
            double propertyValue = m.getProperty(propertyName, double.class);

            if(propertyValue > highest) {
                highest = propertyValue;
            }
        }

        return highest;
    }

    /**
     * Gets the minimum numeric value from a collection by the given property
     * @param propertyName The name of the property to get the minimum value from
     * @return A {@code double} of the minimum value present in the collection from the given property
     */
    public double min(String propertyName) {
        if(collection.isEmpty()) {
            return 0;
        }

        double lowest = Double.MAX_VALUE;

        for(T m : collection) {
            double propertyValue = m.getProperty(propertyName, double.class);

            if(propertyValue < lowest) {
                lowest = propertyValue;
            }
        }

        return lowest;
    }

    /**
     * Gets a list containing entries of just the given property
     * @param propertyName The name of the property to create a list from
     * @param <S> The type of the entries contained within the list
     * @return A new {@link List} object containing all entries from the collection with the given property
     */
    public <S> List<S> select(String propertyName) {
        List<S> list = new ArrayList<>();

        for(T m : collection) {
            //noinspection unchecked
            list.add((S) m.getProperty(propertyName));
        }

        return list;
    }

    /**
     * Sums up the total of the numeric values from the given property
     * @param propertyName The name of the property to get a total sum of
     * @return A {@code double} of the total sum of the numeric values from the given property
     */
    public double sum(String propertyName) {
        double sum = 0;

        for(T m : collection) {
            double propertyValue = m.getProperty(propertyName, double.class);
            sum += propertyValue;
        }

        return sum;
    }

    /**
     * Filters entries from the collection by executing the given predicate on each entry from the collection
     * @param predicate The predicate to execute on each entry from the collection
     * @return A cloned {@link Query} object with the filtering of the predicate applied
     */
    public Query<T> where(Predicate<T> predicate) {
        Query<T> cloned = clone();

        for(T m : collection) {
            if(!predicate.test(m)) {
                cloned.collection.remove(m);
            }
        }

        return cloned;
    }

    /**
     * Converts the collection to an array
     * @param type The type of entries to be contained within the array
     * @return A new {@code array} with the entries of the collection
     */
    public T[] toArray(Class<T> type) {
        //noinspection unchecked
        T[] array = (T[]) Array.newInstance(type, collection.size());
        for(int i = 0; i < array.length; i++) {
            Array.set(array, i, collection.get(i));
        }
        return array;
    }

    /**
     * Converts the collection to a list
     * @return A new {@link List} object with the entries of the collection
     */
    public List<T> toList() {
        return collection;
    }

    /**
     * Clones this instance of a {@link Query} to a new instance with all data persistent
     * @return A new {@link Query} instance with all data persistent
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Query<T> clone() {
        return new Query<>(this);
    }
}
