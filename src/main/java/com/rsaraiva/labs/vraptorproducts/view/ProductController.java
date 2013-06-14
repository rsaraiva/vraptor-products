package com.rsaraiva.labs.vraptorproducts.view;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import com.rsaraiva.labs.vraptorproducts.model.Product;

@Resource
public class ProductController
{

   private EntityManager entityManager;
   private final Result result;

   public ProductController(EntityManager entityManager, Result result)
   {
      this.entityManager = entityManager;
      this.result = result;
   }

   @Get
   public void search()
   {
      result.include("entities", entityManager.createQuery("from Product e").getResultList());
      result.include("categoryOptions", entityManager.createQuery("from Category e").getResultList());

   }

   private Predicate[] getSearchPredicates(Root<Product> root, Product example)
   {
      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();
      String name = example.getName();
      if (name != null && !"".equals(name))
      {
         predicatesList.add(builder.like(root.<String> get("name"), '%' + name + '%'));
      }
      com.rsaraiva.labs.vraptorproducts.model.Category category = example.getCategory();
      if (category != null)
      {
         predicatesList.add(builder.equal(root.get("category"), category));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   @Post
   public void search(Product product)
   {
      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      CriteriaQuery<Product> criteria = builder.createQuery(Product.class);
      Root<Product> root = criteria.from(Product.class);
      TypedQuery<Product> query = this.entityManager.createQuery(criteria.select(root).where(getSearchPredicates(root, product)));
      result.include("entities", query.getResultList());
      result.include("categoryOptions", entityManager.createQuery("from Category e").getResultList());

   }

   public void create()
   {
      result.include("categoryOptions", entityManager.createQuery("from Category e").getResultList());

   }

   @Path("/product/view/{product.id}")
   @Get
   public void view(Product product)
   {
      result.include("product", entityManager.find(Product.class, product.getId()));
   }

   @Path("/product/edit/{product.id}")
   @Get
   public void edit(Product product)
   {
      result.include("product", entityManager.find(Product.class, product.getId()));
      result.include("categoryOptions", entityManager.createQuery("from Category e").getResultList());

   }

   public void save(Product product)
   {
      if (product.getId() == null)
      {
         entityManager.persist(product);
      }
      else
      {
         entityManager.merge(product);
      }
      result.redirectTo(ProductController.class).search();
   }

   public void delete(Product product)
   {
      entityManager.remove(entityManager.find(Product.class, product.getId()));
      result.redirectTo(ProductController.class).search();
   }
}
