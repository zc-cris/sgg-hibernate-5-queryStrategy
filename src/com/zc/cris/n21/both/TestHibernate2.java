package com.zc.cris.n21.both;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class TestHibernate2 {
	
	private SessionFactory sessionFactory = null;
	private Session session = null;
	private Transaction transaction = null;
	
	@Test
	void testManyToOneStrategy() {
//		Order order = this.session.get(Order.class, 1);
//		System.out.println(order.getName());
		
		List<Order> list = this.session.createQuery("from Order o").list();
		for(Order order : list) {
			if(order.getCustomer() != null) {
				System.out.println(order.getCustomer().getName());
			}
		}
		
		//1. lazy取值为proxy或者false分别表示对应的one的一端属性采用延迟检索和立即检索
		
		//2. fetch:join(采用迫切左外连接的方式初始化customer)，忽略lazy属性
		
		//3. batch-size需要设置在1 的那端的class元素中： 
		//<class name="Customer" table="CUSTOMERS" batch-size="5">
		//作用：一次初始化1的这一端的代理对象的个数
	}
	
	
	
	@Test
	void testSetFetch2() {
		
		Customer customer = this.session.get(Customer.class, 1);
		System.out.println(customer.getOrders().size());
		
	}
	
	@Test
	void testSetFetch() {
		
		List<Customer> customers = this.session.createQuery("from Customer").list();
		System.out.println(customers.size());
		
		for(Customer customer : customers) {
			System.out.println(customer.getOrders().size());
		}
		
		//set元素的fetch属性：确定初始化orders集合的方式
		//1. 默认为select，通过正常的方式来初始化set元素
		
		//2. 可以取值为subselect，通过子查询初始化所有的set集合，
		//将子查询作为where 字句的in的条件出现，子查询所有一的一端的id,此时lazy属性有效，但是batch-size失效
		
		//3. 可以取值为join
		//3.1 在加载一的一端的时候（hibernate4.0采用的是迫切左外连接
		//（使用左外连接查询，并且把集合属性初始化）的方式检索n的一端，5.1版本采用普通select查询），忽略lazy属性
		//hql 查询忽略fetch=join的取值，仍然采用懒加载策略
		
	}
	
	@Test
	void testSetBatchSize() {
		
		List<Customer> customers = this.session.createQuery("from Customer").list();
		System.out.println(customers.size());
		
		for(Customer customer : customers) {
			System.out.println(customer.getOrders().size());
		}
		//set元素的batch-size属性可以设定一次初始化set集合的数量
	}
	
	
	@Test
	void testOne2ManyLevelStrategy() {
		Customer customer = this.session.get(Customer.class, 1);
		
		System.out.println(customer.getName());
		
		//强制代理对象初始化
		Hibernate.initialize(customer.getOrders());
		
		// ---------- set的lazy属性 -----------
		//1. 1-n 或者 n-n 的集合属性默认使用懒加载检索策略
		//2. 可以通过设置set的lazy属性为false来修改默认的懒加载检索策略，但是不建议修改，默认为true就好
		//3. lazy还可以设置为extra，即增强的延迟检索策略,尽可能延迟集合初始化的时机,很少使用，了解
		
		System.out.println(customer.getOrders().size());
		
		Order order = new Order();
		order.setId(1);
		System.out.println(customer.getOrders().contains(order));
		
	}
	
	
	@Test
	void testClassLevelStrategy() {
		
		//类级别的懒加载检索策略仅适用load方法，注意不要发生懒加载异常
		Customer customer = this.session.load(Customer.class, 1);
		//com.zc.cris.n21.both.Customer_$$_jvst9f0_0
		System.out.println(customer.getClass().getName());
		
		//不会发送sql语句，而是直接将代理对象的主键打印出来（1）
		System.out.println(customer.getId());
	}
	
	
	
	
	/**
	 * 
	 * @MethodName: init
	 * @Description: TODO (执行每次@Test 方法前需要执行的方法)
	 * @Return Type: void
	 * @Author: zc-cris
	 */
	@BeforeEach		
	public void init() {
		
		 //在5.1.0版本汇总，hibernate则采用如下新方式获取：
	    //1. 配置类型安全的准服务注册类，这是当前应用的单例对象，不作修改，所以声明为final
	    //在configure("cfg/hibernate.cfg.xml")方法中，如果不指定资源路径，默认在类路径下寻找名为hibernate.cfg.xml的文件
	    final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("/hibernate.cfg.xml").build();
	    //2. 根据服务注册类创建一个元数据资源集，同时构建元数据并生成该应用唯一（一般情况下）的一个session工厂
	    this.sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
	    this.session = this.sessionFactory.openSession();
	    this.transaction = this.session.beginTransaction();
		
	}
	
	/**
	 * 
	 * @MethodName: destroy
	 * @Description: TODO (执行每次@Test 方法后需要执行的方法注解)
	 * @Return Type: void
	 * @Author: zc-cris
	 */
	@AfterEach		
	public void destroy() {
		
		this.transaction.commit();
		this.session.close();
		this.sessionFactory.close();
	}
	


}
