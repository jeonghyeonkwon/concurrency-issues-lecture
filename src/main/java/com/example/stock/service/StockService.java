package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

//    @Transactional synchronized일때는 주석처리
    /*
    * synchronized 문제점 : 서버가 1대 일 때만 가능하다. 그래서 거의 사용하지 않는다
    * */
//    public synchronized void decrease(Long id, Long quantity){
//        // get stock
//        // 재고 감소
//        // 저장
//
//        Stock stock = stockRepository.findById(id).orElseThrow();
//
//        stock.decrease(quantity);
//
//        stockRepository.saveAndFlush(stock);
//    }
    @Transactional(propagation = Propagation.REQUIRES_NEW) //부모의 트랙젝션과 별도로 새로운 트랙젝션 생성하는 것
    public void decrease(Long id, Long quantity){
        // get stock
        // 재고 감소
        // 저장

        Stock stock = stockRepository.findById(id).orElseThrow();

        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}


/*
* MySQL로 해결하는 방법
* 1. Pessimistic Lock (충돌이 빈번한게 일어나는 경우)
*   실제 데이터에 Lock 을 걸어서 정합성을 맞추는 방법입니다. exclusive lock을 걸게 되면 다른 트랜잭션에서는
*   lock 이 해제되기전에 데이터를 가져갈 수 없게 된다.
*   데드락이 걸릴 수 있기 때문에 주의하여 사용하여야 한다.
*
* 2. Optimistic Lock
*   실제로 Lock을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법.
*   먼저 데이터를 읽은 후에 update를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하여
*   업데이트 한다. 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은 후 작업을 수행
*   장점 : Pessimistic Lock 처럼 별도의 Lock을 잡지 않으므로 성능상 이점은 있다
*   단점 : update에 관한 실패 로직을 개발자가 직접 작성해야 한다.
*
* 3. Named Lock
*   이름을 가진 metadata locking. 이름을 가진 lock을 취득한 후 해제할때까지 다른 세션은 이 lock을 획득할
*   수 없도록 한다. 주의할 점으로는 transaction이 종료될 때 lock이 자동으로 해제되지 않습니다.
*   별도의 명령어로 해제를 수행해주거나 선점 시간이 끝나야 해제됩니다.
*
* */

/*
* Redis 사용
* Lettuce
* 1. Lettuce
*   - setnx 명령어를 활용하여 분산락 구현
*   - spin lock 방식
*   - lock 이 있다면 실패 없다면 성공
*   - 구현이 간단하다
*   - spring data redis를 이용하면 lettuce가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 된다
*   - spin lock 방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수 있다
*
*
* 2. Redisson
*   - pub-sub 기반으로 Lock 구현 제공
*   - 채널을 통해 획득 내용 전달
*   - 락 획득 재시도를 기본으로 제공한다
*   - pub-sub 방식으로 구현되어 있기 때문에 lettuce와 비교했을 때 redis 에 부하가 덜 간다.
*   - 별도의 라이브러리를 사용해야 한다
*   - lock을 라이브러리 차원에서 제공해주기 때문에 사용법을 공부해야 한다
*
*
*   실무에서는 ?
*   - 재시도가 필요하지 않은 lock은 lettuce 활용
*   - 재시도가 필요한 경우에는 redission를 활용
* * */