Это структурный паттерн, который часто используется в Spring. Суть этого паттерна заключается в том, что по сути создается такой же объект, но сам Proxy выступает в качестве посредника между клиентом и оригинальным объектом. Proxy-объект может контролировать доступ к оригинальному объекту, а также реализовывать дополнительную функциональность (но чаще не меняющую бизнес-логику самого объекта) или иметь отложенную инициализацию

Прокси используется для:
	Контроля доступа
	Ленивой загрузки
	Кеширования
	Логирования и аудита

Создавать прокси можно по разному, но в спринг используется в основном только два способа:
	JDK Dynamic Proxy
		Создается путем реализации интерфейса
			Это удобно, потому что нет необходимости реализовывать отдельный класс
			Но не получится создать прокси, если класс не реализует интерфейсы
	CGLIB
		Не требует интерфейсов
		Создается путем генерации нового класса
		Возможно работать с классами напрямую

Spring по-дефолту, если класс реализует хотя бы один интерфейс, попытается использовать JDK Dynamic Proxy
	если это невозможно, Spring будет использовать CGLIB

Пример реализации прокси объекта через CGLIB
```java
@Component
public class PlaceOrderLoggingBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return createProxyBean(bean);
    }


    private Object createProxyBean(Object bean) {
        Class<?> beanClass = bean.getClass();

        if (bean instanceof Customer customer) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(beanClass);
            enhancer.setCallback(new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                    System.out.println(customer.getName() + " place order!");
                    return method.invoke(customer, objects);
                }
            });
            return enhancer.create();
        }
        return bean;
    }
}
```

Пример реализации прокси объекта через JDK Dynamic Proxy:
```java
@Component
public class ConcurrentCallsLimiterBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();

        if (bean instanceof IWaiter) {
            return Proxy.newProxyInstance(beanClass.getClassLoader(), beanClass.getInterfaces(), new InvocationHandler() {
                private final Semaphore semaphore = new Semaphore(2);


                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    try {
                        System.out.println("Acquire..." + semaphore.availablePermits());
                        semaphore.acquire();
                        return method.invoke(bean, args);
                    } finally {
                        semaphore.release();
                        System.out.println("Release...");
                    }
                }
            });
        }
        return bean;
    }
}
```