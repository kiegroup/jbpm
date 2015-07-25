package org.jbpm.query.jpa.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.data.QueryWhere.QueryCriteriaType;
import org.jbpm.query.jpa.service.QueryModificationService;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.internal.query.QueryParameterIdentifiers;

public abstract class QueryCriteriaUtil {
    
    private final Map<Class, Map<String, Attribute>> criteriaAttributes;
    private final AtomicBoolean criteriaAttributesInitialized = new AtomicBoolean(false);

    public QueryCriteriaUtil(Map<Class, Map<String, Attribute>> criteriaAttributes) { 
       this.criteriaAttributes = criteriaAttributes; 
    }
 
    /**
     * The implementation of this method should be synchronized!
     */
    protected abstract boolean initializeCriteriaAttributes();
    
    private Map<Class, Map<String, Attribute>> getCriteriaAttributes() { 
        if( ! criteriaAttributesInitialized.get() ) { 
           if( initializeCriteriaAttributes() ) { 
               criteriaAttributesInitialized.set(true);
           }  else { 
               throw new IllegalStateException("Queries can not be performed if no persistence unit has been initalized!");
           }
        }
        return criteriaAttributes;
    }
    
    // List cast conversion methods -----------------------------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    public static <C,I> List<I> convertListToInterfaceList( List<C>internalResult, Class<I> interfaceType ) {
        List<I> result = new ArrayList<I>(internalResult.size());
        for( C element : internalResult ) { 
           result.add((I) element);
        }
        return result;
    }

    // constructor helper methods -------------------------------------------------------------------------------------------------
    
    protected static void addCriteria( Map<Class, Map<String, Attribute>> criteriaAttributes, String listId, Attribute attr ) {
        Class table = attr.getJavaMember().getDeclaringClass();
        Map<String, Attribute> tableAttrs = criteriaAttributes.get(table);
        if( tableAttrs == null ) {
            tableAttrs = new ConcurrentHashMap<String, Attribute>(1);
            criteriaAttributes.put(table, tableAttrs);
        }
        Attribute previousMapping = tableAttrs.put(listId, attr);
        assert previousMapping == null : "Previous mapping existed for [" + listId + "]!";
    }

    // abstract methods -----------------------------------------------------------------------------------------------------------
   
    protected abstract CriteriaBuilder getCriteriaBuilder();
  
    /**
     * This method does the persistence-related logic related to executing a query. 
     * </p>
     * All implementations of this method should do the following, in approximately the following order:
     * <ol>
     * <li>Get an {@link EntityManager} instance</li> 
     * <li>Join a transaction using the entity manager</li> 
     * <li>Create a {@link Query} from the given {@link CriteriaQuery} instance.</li> 
     * <li>Call the {@link #applyMetaCriteriaToQuery(Query, QueryWhere)} method</li> 
     * <li>Retrieve the result from the {@link Query} instance.</li> 
     * <li>Close the transaction created, and the created {@link EntityManager} instance. 
     * <li>Return the query result</li> 
     * </ol>
     * 
     * @param queryWhere The {@link QueryWhere} instance containing the meta criteria information. 
     * @param criteriaQuery The created and filled {@link CriteriaQuery} instance
     * @param resultType The type of the {@link List} returned: the query result type.
     * @return A {@link List} of instances, representing the query result.
     */
    // @formatter:off
    protected abstract <T> List<T> createQueryAndApplyMetaCriteriaAndGetResult(
            QueryWhere queryWhere, 
            CriteriaQuery<T> criteriaQuery, 
            Class<T> resultType);
    // @formatter:on
   
    /**
     * Some criteria do not directly refer to a field, such as those stored 
     * in the criteria attributes {@link Map<Class, Map<String, Attribute>>} passed
     * as an argument to the constructor. 
     * </p>
     * For example, the {@link QueryParameterIdentifiers#LAST_VARIABLE_LIST} criteria specifies
     * that only the most recent {@link VariableInstanceLog} should be retrieved. 
     * </p>
     * This method is called from the {@link #createPredicateFromSingleCriteria(CriteriaQuery, QueryCriteria, CriteriaBuilder, Class)}
     * method when no {@link Attribute} instance can be found in the 
     * instances criteria attributes {@link Map<Class, Map<String, Attribute>>}.
     * </p>
     * @param criteriaQuery The {@link CriteriaQuery} instance.
     * @param criteria The {@link QueryCriteria} instance with the criteria information.
     * @param criteriaBuilder The {@link CriteriaBuilder} instance used to help create the query predicate.
     * @param table The {@link Root} instance on which the query is being performed
     * @param resultType The {@link Class} of the result being request.
     * @return A {@link Predicate} representin the information in the {@link QueryCriteria} instance.
     */
    // @formatter:off
    protected abstract <R,T> Predicate createImplementationSpecificCriteria(
            CriteriaQuery<R> criteriaQuery, 
            QueryCriteria criteria, 
            CriteriaBuilder criteriaBuilder, 
            Root<?> table, 
            Class<T> resultType );
    // @formatter:on

    // query logic ----------------------------------------------------------------------------------------------------------------
    
    /**
     * This method takes the high-level steps needed in order to create a JPA {@link CriteriaQuery}. 
     * <ol>
     * <li>A {@link CriteriaBuilder} and {@link CriteriaQuery} instance are created.</li>
     * <li>The tables being selected from are defined in the query.</li>
     * <li>If there are {@link QueryModificationService} instances available, they will be called to add 
     * other tables to the query</li>
     * <li>The {@link CriteriaQuery} instance is filled using the criteria in the {@link QueryWhere} instance</li>
     * <li>A JPA {@link Query} instance is created</li>
     * <li>The meta criteria (max results, offset) are applied to the query</li>
     * <li>The results are retrieved and returned</li>
     * </ol>
     * @param queryWhere a {@link QueryWhere} instance containing the query criteria
     * @param resultType The type ({@link Class}) of the result 
     * @return The result of the query, a {@link List}.
     */
    public <T> List<T> doCriteriaQuery( QueryWhere queryWhere, Class<T> resultType ) {
        // 1. create builder and query instances
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = builder.createQuery(resultType);
        
        // query base;
        criteriaQuery.select(criteriaQuery.from(resultType));
     
        // 1. add other tables (used in kie-remote-services to cross-query on variables, etc.. )
        ServiceLoader<QueryModificationService> queryModServiceLdr = ServiceLoader.load(QueryModificationService.class);
        for( QueryModificationService queryModService : queryModServiceLdr ) {
            queryModService.addTablesToQuery(queryWhere, criteriaQuery, resultType);
        }
        
        fillCriteriaQuery(criteriaQuery, queryWhere, builder, resultType);

        List<T> result = createQueryAndApplyMetaCriteriaAndGetResult(queryWhere, criteriaQuery, resultType);

        return result;
    }

    /**
     * This is a 
     * @param criteriaQuery
     * @param queryWhere
     * @param criteriaBuilder
     * @param resultType
     */
    @SuppressWarnings("unchecked")
    protected <R,T> void fillCriteriaQuery( CriteriaQuery<R> criteriaQuery, QueryWhere queryWhere, CriteriaBuilder criteriaBuilder, Class<T> resultType ) {

        Predicate queryPredicate = createPredicateFromCriteriaList(criteriaQuery, queryWhere.getCriteria(), criteriaBuilder, resultType);
        
        if( queryPredicate != null ) { 
            criteriaQuery.where(queryPredicate);
        }
                
        // 2. add extended criteria
        ServiceLoader<QueryModificationService> queryModServiceLdr = ServiceLoader.load(QueryModificationService.class);
        for( QueryModificationService queryModService : queryModServiceLdr ) {
            queryModService.addCriteriaToQuery(queryWhere, criteriaQuery, criteriaBuilder, resultType);
        }
      
        // 3. Order by
        if( queryWhere.getAscOrDesc() != null ) { 
            String orderByListId = queryWhere.getOrderByListId();
            assert orderByListId != null : "Ascending boolean is set but no order by list Id has been specified!";
            Attribute field = getCriteriaAttributes().get(resultType).get(orderByListId);
            assert field != null : "No Attribute found for order-by listId " + orderByListId
                    + " for result type " + resultType.getSimpleName();
            Root table = null;
            for( Root<?> root : criteriaQuery.getRoots() ) {
                if( root.getJavaType().equals(resultType) ) {
                    table = root;
                    break;
                }
            }
            assert table != null : "Unable to find proper table (Root) instance in query for result type " + resultType.getSimpleName();
           
            Path orderByPath;
            if( field instanceof SingularAttribute ) { 
                orderByPath = table.get((SingularAttribute) field);
            } else { 
                throw new UnsupportedOperationException("Ordering by a join field is not supported!");
            }
            Order order;
            if( queryWhere.getAscOrDesc() ) { 
               order = criteriaBuilder.asc(orderByPath);
            } else { 
               order = criteriaBuilder.desc(orderByPath);
            }
            criteriaQuery.orderBy(order);
        }
    }
  
    /**
     * This method is contains the setup steps for creating and assembling {@link Predicate} instances 
     * from the information in a {@link List} of {@link QueryCriteria} instances. 
     * </p>
     * The steps taken when assembling a {@link Predicate} are the following: 
     * <ol>
     * <li>Separate the given {@link List} of {@link QueryCriteria} into an intersection and disjunction (union) list.</li>
     * <li>Combine separate "range" {@link QueryCriteria} that apply to the same listId</li>
     * <li>Call the {@link #createPredicateFromCriteriaList(CriteriaQuery, List, CriteriaBuilder, Class, boolean)} 
     * method on disjunction criteria list and on the intersection criteria list</li>
     * <li>Take the result of the previous step and appropriately combine the returned {@link Predicate} instances into a 
     * final {@link Predicate} instance that is then returned.</li>
     * </ol> 
     * @param query The {@link CriteriaQuery} instance that we're assembling {@link Predicate} instances for
     * @param inputCriteriaList The list of {@link QueryCriteria} instances that will be processed
     * @param builder A {@link CriteriaBuilder} instance to help us build {@link Predicate} instances
     * @param resultType The {@link Class} (type) of the result, given so that later methods can use it
     * @return A {@link Predicate} instance based on the given {@link QueryCriteria} list
     */
    private <R,T> Predicate createPredicateFromCriteriaList(CriteriaQuery<R> query, List<QueryCriteria> inputCriteriaList, CriteriaBuilder builder, Class<T> resultType ) {
        Predicate queryPredicate = null;
        if( inputCriteriaList.size() > 1 ) { 
            
            List<Predicate> predicateList = new LinkedList<Predicate>();
            QueryCriteria previousCriteria = null;
            QueryCriteria firstCriteria = null;
            List<QueryCriteria> currentIntersectingCriteriaList = new LinkedList<QueryCriteria>();
            
            for( QueryCriteria criteria : inputCriteriaList ) {
                if( criteria.isFirst() ) { 
                   firstCriteria = previousCriteria = criteria;
                   continue;
                } else if( firstCriteria != null ) { 
                    if( criteria.isUnion() ) { 
                       Predicate predicate = createPredicateFromCriteria(query, previousCriteria, builder, resultType);
                       predicateList.add(predicate);
                    } else { 
                        currentIntersectingCriteriaList.add(firstCriteria);
                    }
                    firstCriteria = null;
                }
                
                if( criteria.isUnion() ) { 
                    // AND has precedence over OR: 
                    // If 'criteria' is now OR and there was a list (currentIntersectingCriteriaList) of AND criteria before 'criteria'
                    // - create a predicate from the AND criteria
                    if( previousCriteria != null && ! previousCriteria.isUnion() && ! currentIntersectingCriteriaList.isEmpty() ) { 
                        Predicate predicate 
                            = createPredicateFromIntersectingCriteriaList(query, currentIntersectingCriteriaList, builder, resultType);
                        assert predicate != null : "Null predicate when evaluating intersecting criteria [" + criteria.toString() + "]";
                        predicateList.add(predicate);
                        
                        // - new (empty) current intersecting criteria list
                        currentIntersectingCriteriaList = new LinkedList<QueryCriteria>();
                    }
                    
                    // Process the current union criteria
                    Predicate predicate = createPredicateFromCriteria(query, criteria, builder, resultType);
                    assert predicate != null : "Null predicate when evaluating union criteria [" + criteria.toString() + "]";
                    predicateList.add(predicate);
                } else { 
                    currentIntersectingCriteriaList.add(criteria);
                }

                previousCriteria = criteria; 
            }

            if( ! currentIntersectingCriteriaList.isEmpty() ) { 
                Predicate predicate 
                    = createPredicateFromIntersectingCriteriaList(query, currentIntersectingCriteriaList, builder, resultType);
                predicateList.add(predicate);
            }
            
            assert ! predicateList.isEmpty() : "The predicate list should not (can not?) be empty here!";
            if( predicateList.size() == 1 ) { 
                queryPredicate = predicateList.get(0);
            } else { 
                Predicate [] predicates = predicateList.toArray(new Predicate[predicateList.size()]);
                queryPredicate = builder.or(predicates);
            }
        } else if( inputCriteriaList.size() == 1 ) { 
            QueryCriteria singleCriteria = inputCriteriaList.get(0);
            queryPredicate = createPredicateFromCriteria(query, singleCriteria, builder, resultType);
        }

        return queryPredicate;
    }

    private <R,T> Predicate createPredicateFromCriteria(CriteriaQuery<R> query, QueryCriteria singleCriteria, CriteriaBuilder builder, Class<T> resultType ) {
        Predicate predicate;
        if( singleCriteria.isGroupCriteria() ) { 
            assert ! singleCriteria.hasValues() : "Criteria has both subcriteria (group criteria) and values! [" + singleCriteria.toString() + "]";
            predicate = createPredicateFromCriteriaList(query, singleCriteria.getCriteria(), builder, resultType);
        } else { 
            assert ! singleCriteria.hasCriteria() : "Criteria has both values and subcriteria (group criteria)! [" + singleCriteria.toString() + "]";
            predicate = createPredicateFromSingleCriteria(query, singleCriteria, builder, resultType);
        } 
        return predicate;
    }
    
    private <R,T> Predicate createPredicateFromIntersectingCriteriaList(CriteriaQuery<R> query, List<QueryCriteria> intersectingCriteriaList, CriteriaBuilder builder, Class<T> resultType ) {
        combineIntersectingRangeCriteria(intersectingCriteriaList);
        assert intersectingCriteriaList.size() > 0 : "Empty list of currently intersecting criteria!";
        Predicate [] intersectingPredicates = new Predicate[intersectingCriteriaList.size()];
        int i = 0;
        for( QueryCriteria intersectingCriteria : intersectingCriteriaList ) { 
            Predicate predicate = createPredicateFromCriteria(query, intersectingCriteria, builder, resultType);
            assert predicate != null : "Null predicate when evaluating individual intersecting criteria [" + intersectingCriteria.toString() + "]";
            intersectingPredicates[i++] = predicate;
        }
        
        Predicate predicate;
        if( intersectingPredicates.length > 1 ) { 
            predicate = builder.and(intersectingPredicates);
        } else { 
           predicate = intersectingPredicates[0]; 
        } 
        
        return predicate;
    }

    private void combineIntersectingRangeCriteria(List<QueryCriteria> intersectionCriteria) { 
        Map<String, QueryCriteria> intersectingRangeCriteria = new HashMap<String, QueryCriteria>();
        Iterator<QueryCriteria> iter = intersectionCriteria.iterator();
        while( iter.hasNext() ) { 
            QueryCriteria criteria = iter.next();
            if( QueryCriteriaType.RANGE.equals(criteria.getType()) ) { 
                QueryCriteria previousCriteria = intersectingRangeCriteria.put(criteria.getListId(), criteria);
                if( previousCriteria != null ) { 
                    Object [] prevCritValues, thisCritValues;
                    assert previousCriteria.hasValues() || previousCriteria.hasDateValues() : 
                        "Previous criteria has neither values nor date values!";
                    assert !(previousCriteria.hasValues() && previousCriteria.hasDateValues()) : 
                        "Previous criteria has BOTH values and date values!";
                    assert (previousCriteria.hasValues() && criteria.hasValues()) 
                    || (previousCriteria.hasDateValues() && criteria.hasDateValues()) : 
                        "Previous and current criteria should have either both have values or both have date values!";
                    
                    boolean dateValues = false;
                    if( previousCriteria.hasValues() ) { 
                        prevCritValues = previousCriteria.getValues().toArray();
                        thisCritValues = criteria.getValues().toArray();
                    } else {
                        dateValues = true;
                        prevCritValues = previousCriteria.getDateValues().toArray();
                        thisCritValues = criteria.getDateValues().toArray();
                    } 
                    
                    List values = dateValues ? previousCriteria.getDateValues() : previousCriteria.getValues();
                    if( prevCritValues[0] == null && thisCritValues[1] == null ) { 
                        values.set(0, thisCritValues[0]);
                        iter.remove();
                    } else if( prevCritValues[1] == null && thisCritValues[0] == null ) { 
                        values.set(1, thisCritValues[1]);
                        iter.remove();
                    }
                }
            } 
        }
    }
    
    /**
     * This method is the main method for creating a {@link Predicate} from a {@link QueryCriteria} instance. 
     * </p>
     * If it can not figure out how to create a {@link Predicate} from the given {@link QueryCriteria} instance, 
     * then the (abstract) {@link #createImplementationSpecificCriteria(CriteriaQuery, QueryCriteria, CriteriaBuilder, Root, Class)}
     * method is called. 
     * 
     * @param query
     * @param criteria
     * @param builder
     * @param resultType
     * @return
     */
    @SuppressWarnings("unchecked")
    private <R,T> Predicate createPredicateFromSingleCriteria(CriteriaQuery<R> query, QueryCriteria criteria, CriteriaBuilder builder, Class<T> resultType ) {
        Predicate predicate = null;
        assert criteria.hasValues() || criteria.hasDateValues() : "No values present for criteria with list id: [" + criteria.getListId() + "]";

        Root<?> table = null;
        Expression entityField = null;
        for( Root<?> root : query.getRoots() ) {
            if( root.getJavaType().equals(resultType) ) {
                table = root;
                Attribute attr = getCriteriaAttributes().get(resultType).get(criteria.getListId());
                if( attr != null ) { 
                    if( attr instanceof SingularAttribute ) {
                        entityField = root.get((SingularAttribute) attr);
                    } else if( attr instanceof PluralAttribute ) { 
                        entityField = root.get((PluralAttribute) attr);
                    } else { 
                        throw new IllegalStateException("Unexpected attribute type when processing criteria with list id " + criteria.getListId() + ": " + attr.getClass().getName() );
                    }
                }
                break;
            }
        }
        
        if( entityField != null ) { 
            List<Object> parameters = criteria.getParameters();
            int numParameters = parameters.size();
            assert ! parameters.isEmpty() : "Empty parameters for criteria [" + criteria.toString() + "]";
            switch ( criteria.getType() ) {
            case NORMAL:
                if( numParameters == 1 ) {
                    Object parameter = parameters.get(0);
                    assert parameter != null : "Null parameter for criteria [" + criteria.toString() + "]";
                    predicate = builder.equal(entityField, parameter);
                } else {
                    assert parameters.get(0) != null : "Null 1rst parameter for criteria [" + criteria.toString() + "]";
                    assert parameters.get(parameters.size()-1) != null : "Null last parameter for criteria [" + criteria.toString() + "]";
                    predicate = entityField.in(parameters);
                }
                break;
            case REGEXP:
                List<Predicate> predicateList = new ArrayList<Predicate>();
                for( Object param : parameters ) { 
                    assert param != null : "Null regular expression parameter for criteria [" + criteria.toString() + "]";
                    String likeRegex = convertRegexToJPALikeExpression((String) param );
                    Predicate regexPredicate = builder.like(entityField, likeRegex);
                    predicateList.add(regexPredicate);
                }
                if( predicateList.size() == 1 ) { 
                    predicate = predicateList.get(0);
                } else { 
                    Predicate [] predicates = predicateList.toArray(new Predicate[predicateList.size()]);
                    if( criteria.isUnion() ) { 
                        predicate = builder.or(predicates);
                    } else { 
                        predicate = builder.and(predicates);
                    }
                }
                break;
            case RANGE:
                assert numParameters > 0 && numParameters < 3: "Range expressions may only contain between 1 and 2 parameters, not " + numParameters + " [" + criteria.toString() + "]";
                Object [] rangeObjArr = parameters.toArray();
                Class rangeType = rangeObjArr[0] != null ? rangeObjArr[0].getClass() : rangeObjArr[1].getClass();
                predicate = createRangePredicate( builder, entityField, rangeObjArr[0], rangeObjArr[1], rangeType);
                break;
            default:
                throw new IllegalStateException("Unknown criteria type: " + criteria.getType());
            }
            assert predicate != null : "No predicate created "
                    + "when evaluating " + criteria.getType().toString().toLowerCase() + " criteria "
                    + "[" + criteria.toString() + "]";
        } else { 
           predicate = createImplementationSpecificCriteria(query, criteria, builder, table, resultType );
        }
        
        return predicate;
    }

    protected String convertRegexToJPALikeExpression(String regexInput) { 
        return regexInput.replace('*', '%').replace('.', '_');
    }

    @SuppressWarnings("unchecked")
    private <Y extends Comparable<? super Y>> Predicate createRangePredicate( CriteriaBuilder builder, Expression field, Object start, Object end, Class<Y> rangeType ) { 
        if( start != null && end != null ) { 
            // TODO :asserts!
            return builder.between(field, (Y) start, (Y) end);
        } else if ( start != null ) { 
            return builder.greaterThanOrEqualTo(field, (Y) start);
        } else { 
            return builder.lessThanOrEqualTo(field, (Y) end);
        }
    }
     
    protected void applyMetaCriteriaToQuery(Query query, QueryWhere queryWhere) { 
        if( queryWhere.getCount() != null ) { 
           query.setMaxResults(queryWhere.getCount());
        }
        if( queryWhere.getOffset() != null ) { 
           query.setFirstResult(queryWhere.getOffset());
        } 
    }
    
}
