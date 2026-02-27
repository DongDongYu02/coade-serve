package cn.dong.nexus.core.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectionVO<K, V> {

    private K id;
    private V text;
}
