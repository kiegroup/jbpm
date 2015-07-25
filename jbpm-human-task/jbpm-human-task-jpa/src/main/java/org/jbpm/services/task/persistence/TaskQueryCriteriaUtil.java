package org.jbpm.services.task.persistence;

import static org.kie.internal.query.QueryParameterIdentifiers.ACTUAL_OWNER_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.ARCHIVED;
import static org.kie.internal.query.QueryParameterIdentifiers.BUSINESS_ADMIN_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.CREATED_BY_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.CREATED_ON_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.DEPLOYMENT_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.EXPIRATION_TIME_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.POTENTIAL_OWNER_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_INSTANCE_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_SESSION_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.SKIPPABLE;
import static org.kie.internal.query.QueryParameterIdentifiers.STAKEHOLDER_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.SUB_TASKS_STRATEGY;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_ACTIVATION_TIME_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_DESCRIPTION_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_FORM_NAME_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_NAME_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_PARENT_ID_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_STATUS_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_SUBJECT_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TASK_USER_ROLES_LIMIT_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.TYPE_LIST;
import static org.kie.internal.query.QueryParameterIdentifiers.WORK_ITEM_ID_LIST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;

import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.data.QueryWhere.QueryCriteriaType;
import org.jbpm.query.jpa.impl.QueryCriteriaUtil;
import org.jbpm.query.jpa.service.QueryModificationService;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl_;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl_;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl_;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.TaskImpl_;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.impl.model.UserImpl_;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.query.QueryParameterIdentifiers;

public class TaskQueryCriteriaUtil extends QueryCriteriaUtil {
    
    // Query Field Info -----------------------------------------------------------------------------------------------------------
    
    public final static Map<Class, Map<String, Attribute>> criteriaAttributes 
        = new ConcurrentHashMap<Class, Map<String, Attribute>>();

    @Override
    protected synchronized boolean initializeCriteriaAttributes() { 
        if( TaskImpl_.id == null ) { 
            // EMF/persistence has not been initialized: 
            // When a persistence unit (EntityManagerFactory) is initialized, 
            // the fields of classes annotated with @StaticMetamodel are filled using reflection
            return false;
        }
        // do not do initialization twice (slow performance, otherwise it doesn't matter)
        if( ! criteriaAttributes.isEmpty() ) { 
            return true; 
        }
            
        // TaskImpl
        addCriteria(criteriaAttributes, TASK_ACTIVATION_TIME_LIST, TaskDataImpl_.activationTime);
        addCriteria(criteriaAttributes, ACTUAL_OWNER_ID_LIST, TaskDataImpl_.actualOwner);
        addCriteria(criteriaAttributes, ARCHIVED, TaskImpl_.archived);
        addCriteria(criteriaAttributes, BUSINESS_ADMIN_ID_LIST, PeopleAssignmentsImpl_.businessAdministrators);
        addCriteria(criteriaAttributes, CREATED_BY_LIST, TaskDataImpl_.createdBy); // initiator
        addCriteria(criteriaAttributes, CREATED_ON_LIST, TaskDataImpl_.createdOn);
        addCriteria(criteriaAttributes, DEPLOYMENT_ID_LIST, TaskDataImpl_.deploymentId);
        addCriteria(criteriaAttributes, TASK_DESCRIPTION_LIST, TaskImpl_.descriptions);
        addCriteria(criteriaAttributes, EXPIRATION_TIME_LIST, TaskDataImpl_.expirationTime);
        addCriteria(criteriaAttributes, TASK_FORM_NAME_LIST, TaskImpl_.formName);
        addCriteria(criteriaAttributes, TASK_NAME_LIST, TaskImpl_.names);
        addCriteria(criteriaAttributes, POTENTIAL_OWNER_ID_LIST, PeopleAssignmentsImpl_.potentialOwners);
        addCriteria(criteriaAttributes, PROCESS_ID_LIST, TaskDataImpl_.processId); 
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_ID_LIST, TaskDataImpl_.processInstanceId); 
        addCriteria(criteriaAttributes, PROCESS_SESSION_ID_LIST, TaskDataImpl_.processSessionId); 
        addCriteria(criteriaAttributes, SKIPPABLE, TaskDataImpl_.skipable); 
        addCriteria(criteriaAttributes, STAKEHOLDER_ID_LIST, PeopleAssignmentsImpl_.taskStakeholders);
        addCriteria(criteriaAttributes, TASK_STATUS_LIST, TaskDataImpl_.status);
        addCriteria(criteriaAttributes, TASK_SUBJECT_LIST, TaskImpl_.subjects);
        addCriteria(criteriaAttributes, SUB_TASKS_STRATEGY, TaskImpl_.subTaskStrategy);
        addCriteria(criteriaAttributes, TASK_ID_LIST, TaskImpl_.id);
        addCriteria(criteriaAttributes, TASK_PARENT_ID_LIST, TaskDataImpl_.parentId);
        addCriteria(criteriaAttributes, TYPE_LIST, TaskImpl_.taskType);
        addCriteria(criteriaAttributes, WORK_ITEM_ID_LIST, TaskDataImpl_.workItemId);
        
        return true;
    }
   
    // Implementation specific logic ----------------------------------------------------------------------------------------------
    
    private JPATaskPersistenceContext taskQueryService;
    
    public TaskQueryCriteriaUtil(JPATaskPersistenceContext persistenceContext) { 
        super(criteriaAttributes);
        this.taskQueryService = persistenceContext;
    }
  
    private EntityManager getEntityManager() { 
        return this.taskQueryService.getEntityManager();
    }
  
    private void joinTransaction() { 
        this.taskQueryService.joinTransaction();
    }
   
    protected CriteriaBuilder getCriteriaBuilder() { 
        return getEntityManager().getCriteriaBuilder();
    }

    // Implementation specific methods --------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<TaskSummaryImpl> doCriteriaQuery(String userId, UserGroupCallback userGroupCallback, QueryWhere queryWhere) { 
       
        // 1. create builder and query instances
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<TaskSummaryImpl> criteriaQuery = builder.createQuery(TaskSummaryImpl.class);
      
        // 2. query base
        Root<TaskImpl> taskRoot = criteriaQuery.from(TaskImpl.class);
        Join taskDataJoin = taskRoot.join(TaskImpl_.taskData);
        Selection select = builder.construct(TaskSummaryImpl.class, 
                  taskRoot.get(TaskImpl_.id), 
                  taskRoot.get(TaskImpl_.name), 
                  taskRoot.get(TaskImpl_.subject), 
                  taskRoot.get(TaskImpl_.description), 
                  taskDataJoin.get(TaskDataImpl_.status), 
                  taskRoot.get(TaskImpl_.priority), 
                  taskDataJoin.get(TaskDataImpl_.skipable), 
                  taskDataJoin.get(TaskDataImpl_.actualOwner), 
                  taskDataJoin.get(TaskDataImpl_.createdBy), 
                  taskDataJoin.get(TaskDataImpl_.createdOn), 
                  taskDataJoin.get(TaskDataImpl_.activationTime), 
                  taskDataJoin.get(TaskDataImpl_.expirationTime), 
                  taskDataJoin.get(TaskDataImpl_.processId), 
                  taskDataJoin.get(TaskDataImpl_.processSessionId), 
                  taskDataJoin.get(TaskDataImpl_.processInstanceId), 
                  taskDataJoin.get(TaskDataImpl_.deploymentId), 
                  taskRoot.get(TaskImpl_.subTaskStrategy), 
                  taskDataJoin.get(TaskDataImpl_.parentId));
        criteriaQuery.select(select);

        // 3. add other tables (used in kie-remote-services to cross-query on variables, etc.. )
        ServiceLoader<QueryModificationService> queryModServiceLdr = ServiceLoader.load(QueryModificationService.class);
        for( QueryModificationService queryModService : queryModServiceLdr ) {
            queryModService.addTablesToQuery(queryWhere, criteriaQuery, TaskSummaryImpl.class);
        }
   
        checkExistingCriteriaForUserBasedLimit(queryWhere, userId, userGroupCallback);
        
        // 4. process query criteria 
        fillCriteriaQuery(criteriaQuery, queryWhere, builder, TaskImpl.class);
        
        // 5. retrieve result (after also applying meta-criteria)
        List<TaskSummaryImpl> result = createQueryAndApplyMetaCriteriaAndGetResult(queryWhere, criteriaQuery, TaskSummaryImpl.class);

        return result;
    } 
    
    private void checkExistingCriteriaForUserBasedLimit(QueryWhere queryWhere, String userId, UserGroupCallback userGroupCallback) { 
        if( criteriaListForcesUserLimitation(queryWhere.getCriteria()) ) { 
            List<String> groupIds = userGroupCallback.getGroupsForUser(userId, null, null);
            addUserRolesLimitCriteria(queryWhere, userId, groupIds);
        }
    }
 
    private static Set<String> taskUserRoleLimitingListIds = new HashSet<String>();
    static { 
        taskUserRoleLimitingListIds.add(ACTUAL_OWNER_ID_LIST);
        taskUserRoleLimitingListIds.add(BUSINESS_ADMIN_ID_LIST);
        taskUserRoleLimitingListIds.add(CREATED_BY_LIST);
        taskUserRoleLimitingListIds.add(POTENTIAL_OWNER_ID_LIST);
        taskUserRoleLimitingListIds.add(STAKEHOLDER_ID_LIST);
    }
    
    private static boolean criteriaListForcesUserLimitation(List<QueryCriteria> criteriaList) { 
        boolean userLimitiationIntersection = true;
        for( QueryCriteria criteria : criteriaList ) { 
          if( criteria.isUnion() ) { 
              return false;
          }
          if( criteria.isGroupCriteria() ) { 
              if( criteriaListForcesUserLimitation(criteria.getCriteria()) ) { 
                  return true;
              }
              continue;
          }
          // intersection criteria
          if( taskUserRoleLimitingListIds.contains(criteria.getListId()) ) { 
             return true; 
          }
        }
        return userLimitiationIntersection;
    }
    
    private void addUserRolesLimitCriteria( QueryWhere queryWhere, String userId, List<String> groupIds ) {
        List<QueryCriteria> newBaseCriteriaList = new ArrayList<QueryCriteria>(2);
        
        // user role limiting criteria
        QueryCriteria userRolesLimitingCriteria = new QueryCriteria(
                QueryParameterIdentifiers.TASK_USER_ROLES_LIMIT_LIST, 
                false, 
                QueryCriteriaType.NORMAL, 
                2);
        userRolesLimitingCriteria.getValues().add(userId);
        userRolesLimitingCriteria.getValues().add(groupIds);
        newBaseCriteriaList.add(userRolesLimitingCriteria);
        
        // original criteria list in a new group
        if( ! queryWhere.getCriteria().isEmpty() ) { 
            QueryCriteria originalBaseCriteriaGroup = new QueryCriteria(false);
            originalBaseCriteriaGroup.setCriteria(queryWhere.getCriteria());
            newBaseCriteriaList.add(originalBaseCriteriaGroup);
        }
        
        queryWhere.setCriteria(newBaseCriteriaList);
    }

    protected <T> List<T> createQueryAndApplyMetaCriteriaAndGetResult(QueryWhere queryWhere, CriteriaQuery<T> criteriaQuery, Class<T> resultType) { 
        EntityManager em = getEntityManager();
        joinTransaction();
        Query query = em.createQuery(criteriaQuery);
    
        applyMetaCriteriaToQuery(query, queryWhere);
        
        // execute query
        List<T> result = query.getResultList();

        // close em and end tx? This is done *outside* of this class 
        
        return result;
    }

    @Override
    protected <R,T> Predicate createImplementationSpecificCriteria( 
            CriteriaQuery<R> criteriaQuery, 
            QueryCriteria criteria,
            CriteriaBuilder builder, 
            Root<?> table,
            Class<T> resultType ) {
       
        Predicate predicate = null;
        if( TASK_USER_ROLES_LIMIT_LIST.equals(criteria.getListId()) ) { 
            predicate = createTaskUserRolesLimitPredicate(criteria, criteriaQuery, builder);
        } else { 
            throw new IllegalStateException("List id " + criteria.getListId() + " is not supported for queries on " + resultType.getSimpleName() + ".");
        }
        return predicate;
    }

    @SuppressWarnings("unchecked")
    static <T> Predicate createTaskUserRolesLimitPredicate(QueryCriteria criteria, CriteriaQuery<T> criteriaQuery, CriteriaBuilder builder) { 
            String userId = (String) criteria.getValues().get(0);
            List<String> groupIds = (List<String>) criteria.getValues().get(1);
            
        Root<TaskImpl> taskRoot = null;
        for( Root root : criteriaQuery.getRoots() ) { 
            if( root.getJavaType().equals(TaskImpl.class) ) { 
                taskRoot = root;
            }
        }
        assert taskRoot != null : "TaskImpl Root instance could not be found in query!";
        
        Join<TaskImpl,TaskDataImpl> taskDataJoin = null;
        Join<TaskImpl,PeopleAssignmentsImpl> peopleAssignJoin = null;
        Join<TaskDataImpl,UserImpl> createdByJoin = null;
        Join<TaskDataImpl,UserImpl> actualOwnerJoin = null;
        if( taskRoot != null ) { 
            for( Join<TaskImpl,?> join : taskRoot.getJoins() ) { 
                if( join.getJavaType().equals(PeopleAssignmentsImpl.class) ) { 
                    peopleAssignJoin = (Join<TaskImpl, PeopleAssignmentsImpl>) join;
                } else if( join.getJavaType().equals(TaskDataImpl.class) ) { 
                    taskDataJoin = (Join<TaskImpl,TaskDataImpl>) join;
                }
                
            }
        }
        assert taskDataJoin != null : "TaskImpl -> TaskDataImpl join could not be found in query!";
        
        for( Join<TaskDataImpl,?> join : taskDataJoin.getJoins() ) { 
            if( join.getJavaType().equals(UserImpl.class) ) { 
                String joinFieldName = join.getAttribute().getName();
                if( TaskDataImpl_.actualOwner.getName().equals(joinFieldName) ) { 
                    actualOwnerJoin = (Join<TaskDataImpl,UserImpl>) join;
                } else if( TaskDataImpl_.createdBy.getName().equals(joinFieldName) ) { 
                    createdByJoin = (Join<TaskDataImpl,UserImpl>) join;
                }
            }
        }
        if( createdByJoin == null ) { 
            createdByJoin = taskDataJoin.join(TaskDataImpl_.createdBy, JoinType.LEFT);
        } 
        if( actualOwnerJoin == null ) { 
            actualOwnerJoin = taskDataJoin.join(TaskDataImpl_.actualOwner, JoinType.LEFT);
        }
        
        if( peopleAssignJoin == null ) { 
            peopleAssignJoin = taskRoot.join(TaskImpl_.peopleAssignments, JoinType.LEFT);
        }
       
        Join<PeopleAssignmentsImpl,OrganizationalEntityImpl> busAdminsJoin = null;
        Join<PeopleAssignmentsImpl,OrganizationalEntityImpl> potOwnersJoin = null;
        Join<PeopleAssignmentsImpl,OrganizationalEntityImpl> taskStakesJoin = null;

        for( Join<PeopleAssignmentsImpl,?> join : peopleAssignJoin.getJoins() ) {
            String joinFieldName = join.getAttribute().getName();
            if( PeopleAssignmentsImpl_.businessAdministrators.getName().equals(joinFieldName) ) { 
                busAdminsJoin = (Join<PeopleAssignmentsImpl,OrganizationalEntityImpl>) join;
            } else if( PeopleAssignmentsImpl_.potentialOwners.getName().equals(joinFieldName) ) { 
                potOwnersJoin = (Join<PeopleAssignmentsImpl,OrganizationalEntityImpl>) join;
            }  else if( PeopleAssignmentsImpl_.taskStakeholders.getName().equals(joinFieldName) ) { 
                taskStakesJoin = (Join<PeopleAssignmentsImpl,OrganizationalEntityImpl>) join;
            }
        }

        if( busAdminsJoin == null ) { 
            busAdminsJoin = peopleAssignJoin.join(PeopleAssignmentsImpl_.businessAdministrators, JoinType.LEFT);
        }
        if( potOwnersJoin == null ) { 
            potOwnersJoin = peopleAssignJoin.join(PeopleAssignmentsImpl_.potentialOwners, JoinType.LEFT);
        }
        if( taskStakesJoin == null ) { 
            taskStakesJoin = peopleAssignJoin.join(PeopleAssignmentsImpl_.taskStakeholders, JoinType.LEFT);
        }
        assert busAdminsJoin != null : "Could not find business administrators join!";
        assert potOwnersJoin != null : "Could not find potential owners join!";
        assert taskStakesJoin != null : "Could not find task stakeholders join!";
        
       
        Predicate createdByPred = builder.equal(createdByJoin.get(UserImpl_.id), userId);
        Predicate actualOwnPred = builder.equal(actualOwnerJoin.get(UserImpl_.id), userId);
        
        Predicate busAdminPred = busAdminsJoin.get(OrganizationalEntityImpl_.id).in(groupIds);
        Predicate potOwnerPred = potOwnersJoin.get(OrganizationalEntityImpl_.id).in(groupIds);
        Predicate taskStakePred = taskStakesJoin.get(OrganizationalEntityImpl_.id).in(groupIds);
       
        return builder.or(createdByPred, actualOwnPred, busAdminPred, potOwnerPred, taskStakePred);
    }

    
    /**
    @Override
    public List<TaskSummary> query( String userId, QueryData queryData ) {
        // 1a. setup query
        StringBuilder queryBuilder = new StringBuilder(TASKSUMMARY_SELECT).append(TASKSUMMARY_FROM);
        
        // 1b. add other tables (used in kie-remote-services to cross-query on variables, etc.. )
        ServiceLoader<QueryModificationService> queryModServiceLdr = ServiceLoader.load(QueryModificationService.class);
        for( QueryModificationService queryModService : queryModServiceLdr ) { 
           queryModService.addTablesToQuery(queryBuilder, queryData);
        }
       
        // 1c. finish setup
        queryBuilder.append(TASKSUMMARY_WHERE);
        
        Map<String, Object> params = new HashMap<String, Object>();
        QueryAndParameterAppender queryAppender = new QueryAndParameterAppender(queryBuilder, params);
       
        // 2. check to see if we can results if possible by manipulating existing parameters
        GroupIdsCache groupIds = new GroupIdsCache(userId);
        boolean existingParametersUsedToLimitToAllowedResults = useExistingUserGroupIdToLimitResults(userId, queryData, groupIds);
        
        // 3a. add extended criteria 
        for( QueryModificationService queryModService : queryModServiceLdr ) { 
           queryModService.addCriteriaToQuery(queryData, queryAppender);
        }
        
        // 3a. apply normal query parameters
        if( ! queryData.unionParametersAreEmpty() ) { 
            for( Entry<String, List<? extends Object>> paramsEntry : queryData.getUnionParameters().entrySet() ) { 
                String listId = paramsEntry.getKey();
                Class<?> criteriaFieldClass = criteriaFieldClasses.get(listId);
                assert criteriaFieldClass != null : listId + ": criteria field class not found";
                String jpqlField = criteriaFields.get(listId);
                assert jpqlField != null : listId + ": criteria field not found";
                String joinClause = criteriaFieldJoinClauses.get(listId);
                queryAppender.addQueryParameters( paramsEntry.getValue(), listId, criteriaFieldClass, jpqlField, joinClause, true);
            }
        }
        if( ! queryData.intersectParametersAreEmpty() ) { 
            for( Entry<String, List<? extends Object>> paramsEntry : queryData.getIntersectParameters().entrySet() ) { 
                String listId = paramsEntry.getKey();
                Class<?> criteriaFieldClass = criteriaFieldClasses.get(listId);
                QueryAndParameterAppender.debugQueryParametersIdentifiers();
                assert criteriaFieldClass != null : listId + ": criteria field class not found";
                String jpqlField = criteriaFields.get(listId);
                assert jpqlField != null : listId + ": criteria field not found";
                String joinClause = criteriaFieldJoinClauses.get(listId);
                queryAppender.addQueryParameters(paramsEntry.getValue(), listId, criteriaFieldClass, jpqlField, joinClause, false);
            }
        }
        // 3b. apply range query parameters
        if( ! queryData.unionRangeParametersAreEmpty() ) { 
            for( Entry<String, List<? extends Object>> paramsEntry : queryData.getUnionRangeParameters().entrySet() ) { 
                String listId = paramsEntry.getKey();
                Class<?> criteriaFieldClass = criteriaFieldClasses.get(listId);
                assert criteriaFieldClass != null : listId + ": criteria field class not found";
                String jpqlField = criteriaFields.get(listId);
                assert jpqlField != null : listId + ": criteria field not found";
                String joinClause = criteriaFieldJoinClauses.get(listId);
                queryAppender.addRangeQueryParameters(paramsEntry.getValue(), listId, criteriaFieldClass, jpqlField, joinClause, true);
            }
        }
        if( ! queryData.intersectRangeParametersAreEmpty() ) { 
            for( Entry<String, List<? extends Object>> paramsEntry : queryData.getIntersectRangeParameters().entrySet() ) { 
                String listId = paramsEntry.getKey();
                Class<?> criteriaFieldClass = criteriaFieldClasses.get(listId);
                assert criteriaFieldClass != null : listId + ": criteria field class not found";
                String jpqlField = criteriaFields.get(listId);
                assert jpqlField != null : listId + ": criteria field not found";
                String joinClause = criteriaFieldJoinClauses.get(listId);
                queryAppender.addRangeQueryParameters(paramsEntry.getValue(), listId, criteriaFieldClass, jpqlField, joinClause, false);
            }
        }
        // 3c. apply regex query parameters
        if( ! queryData.unionRegexParametersAreEmpty() ) { 
            for( Entry<String, List<String>> paramsEntry : queryData.getUnionRegexParameters().entrySet() ) { 
                String listId = paramsEntry.getKey();
                String jpqlField = criteriaFields.get(listId);
                assert jpqlField != null : listId + ": criteria field not found";
                String joinClause = criteriaFieldJoinClauses.get(listId);
                queryAppender.addRegexQueryParameters(paramsEntry.getValue(), listId, jpqlField, joinClause, true);
            }
        }
        if( ! queryData.intersectRegexParametersAreEmpty() ) { 
            for( Entry<String, List<String>> paramsEntry : queryData.getIntersectRegexParameters().entrySet() ) { 
                String listId = paramsEntry.getKey();
                String jpqlField = criteriaFields.get(listId);
                assert jpqlField != null : listId + ": criteria field not found";
                String joinClause = criteriaFieldJoinClauses.get(listId);
                queryAppender.addRegexQueryParameters(paramsEntry.getValue(), listId, jpqlField, joinClause, false);
            }
        }
       
        // 4. close query clause, if parameters have been applied
        while( queryAppender.getParenthesesNesting() > 0 ) { 
            queryAppender.closeParentheses();
        }
     
        // 5. add "limit tasks to viewable tasks" query if step 2 didn't succeed
        if( ! existingParametersUsedToLimitToAllowedResults ) { 
            addPossibleUserRolesQueryClause( userId, groupIds, params, queryAppender );
        }
      
        // 6. apply meta info: max results, offset, order by, etc 
        String query = queryBuilder.toString();
        applyQueryContext(params, queryData.getQueryContext());
     
        // 7. Run the query!
        return persistenceContext.queryStringWithParametersInTransaction(query, params,
                ClassUtil.<List<TaskSummary>>castClass(List.class));
    }

    // actual owner and created by (initiator) can only be users
    private static String [] userParameterIds = { 
        ACTUAL_OWNER_ID_LIST,
        CREATED_BY_LIST
    };
    // ... but stakeholder, potential and bus. admin can be users orgroups
    private static String [] groupParameterIds = { 
        STAKEHOLDER_ID_LIST,
        POTENTIAL_OWNER_ID_LIST,
        BUSINESS_ADMIN_ID_LIST
    };

    */
        

   
    /**
     * Determine whether or not we need to add a limiting clause 
     * ({@see #addPossibleUserRolesQueryClause(String, StringBuilder, GroupIdsCache, Map, QueryAndParameterAppender)}
     * at the end of the query
     * 
     * @param userGroupParamListIds The parameter list ids that we are looking for
     * @param queryData The query data
     * @param userGroupIds user and group ids from the user who called the query operation
     * @return true if we don't need to add another clause, false if we do
    private boolean useExistingUserGroupIdAsParameter(String [] userGroupParamListIds, QueryData queryData, String... userGroupIds) { 
        for( String listId : userGroupParamListIds ) { 
            List<String> intersectListUserIds = (List<String>) queryData.getIntersectParameters().get(listId);
            if( intersectListUserIds != null ) { 
                for( String groupId : userGroupIds ) { 
                    if( intersectListUserIds.contains(groupId)) {
                        // yes! one of groupIds == one of intersectListUserIds
                        return true;
                    }
                }
            }
        }
        return false;
    }
   
    /**
     * Add a query clause to the end of the query limiting the result to the tasks that the user is allowed to see
     * @param userId the user id
     * @param queryBuilder The {@link StringBuilder} instance with the query string
     * @param groupIdsCache A cache of the user's group ids
     * @param params The params that will be set in the query
     * @param queryAppender The {@link QueryAndParameterAppender} instance being used
    private void addPossibleUserRolesQueryClause(String userId, GroupIdsCache groupIdsCache, Map<String, Object> params, 
            QueryAndParameterAppender queryAppender) { 
       
        // start phrase
        StringBuilder rolesQueryPhraseBuilder = new StringBuilder( "( " );
       
        // add criteria for catching tasks that refer to the user
        String userIdParamName = queryAppender.generateParamName();
        params.put(userIdParamName, userId);
        String groupIdsParamName = queryAppender.generateParamName();
        List<String> userAndGroupIds = new ArrayList<String>(1+groupIdsCache.size());
        userAndGroupIds.add(userId);
        userAndGroupIds.addAll(groupIdsCache.getGroupIds());
        params.put(groupIdsParamName, userAndGroupIds);
        
        rolesQueryPhraseBuilder.append("( ")
            .append("t.taskData.createdBy.id = :").append(userIdParamName).append("\n OR ")
            .append("( stakeHolders.id in :").append(groupIdsParamName).append(" and\n")
            .append("  stakeHolders in elements ( t.peopleAssignments.taskStakeholders ) )").append("\n OR " )
            .append("( potentialOwners.id in :").append(groupIdsParamName).append(" and\n")
            .append("  potentialOwners in elements ( t.peopleAssignments.potentialOwners ) )").append("\n OR " )
            .append("t.taskData.actualOwner.id = :").append(userIdParamName).append("\n OR ")
            .append("( businessAdministrators.id in :").append(groupIdsParamName).append(" and\n")
            .append("  businessAdministrators in elements ( t.peopleAssignments.businessAdministrators ) )")
            .append(" )\n");
        
        rolesQueryPhraseBuilder.append(") ");
       
        queryAppender.addToQueryBuilder(rolesQueryPhraseBuilder.toString(), false);
    }
     */
   
}
